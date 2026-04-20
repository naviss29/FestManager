package com.festmanager.websocket;

import com.festmanager.dto.DashboardSnapshotResponse;
import com.festmanager.entity.Affectation;
import com.festmanager.entity.Mission;
import com.festmanager.entity.enums.StatutAffectation;
import com.festmanager.repository.AffectationRepository;
import com.festmanager.repository.EvenementRepository;
import com.festmanager.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service WebSocket : pousse les mises à jour du dashboard
 * vers les clients abonnés au topic /topic/dashboard/{evenementId}.
 * Fournit aussi le snapshot REST initial.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AffectationRepository affectationRepository;
    private final MissionRepository missionRepository;
    private final EvenementRepository evenementRepository;

    /**
     * Calcule et retourne un snapshot complet des statistiques d'un événement.
     * Appelé une fois au chargement du dashboard, puis les mises à jour arrivent via WebSocket.
     *
     * Stratégie anti-N+1 :
     * - findByEvenementId(UUID) charge les missions avec leurs créneaux en un seul JOIN (@EntityGraph).
     * - La liste est chargée UNE SEULE FOIS et réutilisée pour le count ET les stats par mission.
     *   (Avant correction : findByEvenementId était appelé deux fois et les créneaux étaient chargés lazily.)
     */
    public DashboardSnapshotResponse snapshot(UUID evenementId) {
        var evenement = evenementRepository.findById(evenementId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable"));

        // Chargement unique des missions avec leurs créneaux (évite un double appel SQL et le N+1 sur getCreneaux())
        List<Mission> missions = missionRepository.findByEvenementId(evenementId);
        long nbMissions     = missions.size();

        // Ces counts sont des requêtes SQL agrégées directement en base (pas de chargement d'entités)
        long nbCreneaux     = missionRepository.countCreneauxByEvenement(evenementId);
        long nbPlaces       = missionRepository.sumPlacesRequisesByEvenement(evenementId);
        long nbConfirmes    = affectationRepository.countParEvenementEtStatut(evenementId, StatutAffectation.CONFIRME.name());
        long nbEnAttente    = affectationRepository.countParEvenementEtStatut(evenementId, StatutAffectation.EN_ATTENTE.name());
        long nbRefuses      = affectationRepository.countParEvenementEtStatut(evenementId, StatutAffectation.REFUSE.name());
        long nbAnnules      = affectationRepository.countParEvenementEtStatut(evenementId, StatutAffectation.ANNULE.name());
        long nbBenevoles    = affectationRepository.countBenevolesDistinctsParEvenement(evenementId);
        double taux         = nbPlaces > 0 ? Math.round((double) nbConfirmes / nbPlaces * 1000.0) / 10.0 : 0.0;

        // Pré-calcul des counts confirmés par mission en une seule requête GROUP BY
        // (évite le N+1 : avant = 1 COUNT SQL par mission, maintenant = 1 requête pour toutes)
        List<UUID> missionIds = missions.stream().map(Mission::getId).toList();
        Map<UUID, Long> confirmesParMission = affectationRepository
                .countParMissionsGrouped(missionIds, StatutAffectation.CONFIRME)
                .stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> (Long) row[1]));

        List<DashboardSnapshotResponse.MissionStat> statsParMission = missions.stream()
                .map(m -> missionStat(m, confirmesParMission.getOrDefault(m.getId(), 0L)))
                .toList();

        DashboardSnapshotResponse snapshot = new DashboardSnapshotResponse();
        snapshot.setEvenementId(evenement.getId());
        snapshot.setEvenementNom(evenement.getNom());
        snapshot.setNbMissions(nbMissions);
        snapshot.setNbCreneaux(nbCreneaux);
        snapshot.setNbPlacesRequises(nbPlaces);
        snapshot.setNbBenevolesEngages(nbBenevoles);
        snapshot.setNbConfirmes(nbConfirmes);
        snapshot.setNbEnAttente(nbEnAttente);
        snapshot.setNbRefuses(nbRefuses);
        snapshot.setNbAnnules(nbAnnules);
        snapshot.setTauxRemplissage(taux);
        snapshot.setMissions(statsParMission);
        return snapshot;
    }

    /**
     * Calcule les statistiques d'une mission individuelle pour le dashboard.
     * Le paramètre confirmes est pré-calculé en amont par countParMissionsGrouped()
     * pour éviter le N+1 (avant : 1 COUNT SQL par mission).
     */
    private DashboardSnapshotResponse.MissionStat missionStat(Mission mission, long confirmes) {
        // Les créneaux sont déjà chargés en mémoire (JOIN FETCH dans le repository)
        long places = mission.getCreneaux() != null
            ? mission.getCreneaux().stream().mapToLong(c -> c.getNbBenevolesRequis()).sum()
            : 0;
        double taux = places > 0 ? Math.round((double) confirmes / places * 1000.0) / 10.0 : 0.0;

        DashboardSnapshotResponse.MissionStat stat = new DashboardSnapshotResponse.MissionStat();
        stat.setMissionId(mission.getId());
        stat.setMissionNom(mission.getNom());
        stat.setCategorie(mission.getCategorie());
        stat.setNbPlacesRequises(places);
        stat.setNbConfirmes(confirmes);
        stat.setTauxRemplissage(taux);
        return stat;
    }

    /**
     * Notifie tous les clients WebSocket abonnés au dashboard d'un événement
     * lors d'un changement d'affectation (création, validation, annulation…).
     * Appelé de manière asynchrone depuis AffectationService après chaque opération.
     * Les erreurs WebSocket sont silencieuses pour ne pas bloquer l'opération métier.
     */
    public void notifierAffectation(Affectation affectation, DashboardEvent.TypeEvenement type) {
        try {
            var creneau = affectation.getCreneau();
            var mission = creneau.getMission();
            var evenement = mission.getEvenement();

            int nbAffectes = affectationRepository.countByCreneauIdAndStatut(
                    creneau.getId(), StatutAffectation.CONFIRME);

            DashboardEvent event = new DashboardEvent(
                    type,
                    evenement.getId(),
                    mission.getId(),
                    creneau.getId(),
                    affectation.getBenevole().getId(),
                    mission.getNom(),
                    nbAffectes,
                    creneau.getNbBenevolesRequis(),
                    LocalDateTime.now()
            );

            String destination = "/topic/dashboard/" + evenement.getId();
            messagingTemplate.convertAndSend(destination, event);
            log.debug("Dashboard notifié : {} sur {}", type, destination);

        } catch (Exception e) {
            // Ne jamais bloquer l'opération métier si le WebSocket échoue
            log.warn("Échec notification WebSocket : {}", e.getMessage());
        }
    }
}
