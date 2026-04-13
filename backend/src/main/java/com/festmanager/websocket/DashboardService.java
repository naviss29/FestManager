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
import java.util.UUID;

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
     * Appelé une fois au chargement du dashboard, puis les mises à jour arrivent via WS.
     */
    public DashboardSnapshotResponse snapshot(UUID evenementId) {
        var evenement = evenementRepository.findById(evenementId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable"));

        long nbMissions     = missionRepository.findByEvenementId(evenementId).size();
        long nbCreneaux     = missionRepository.countCreneauxByEvenement(evenementId);
        long nbPlaces       = missionRepository.sumPlacesRequisesByEvenement(evenementId);
        long nbConfirmes    = affectationRepository.countParEvenementEtStatut(evenementId, StatutAffectation.CONFIRME);
        long nbEnAttente    = affectationRepository.countParEvenementEtStatut(evenementId, StatutAffectation.EN_ATTENTE);
        long nbRefuses      = affectationRepository.countParEvenementEtStatut(evenementId, StatutAffectation.REFUSE);
        long nbAnnules      = affectationRepository.countParEvenementEtStatut(evenementId, StatutAffectation.ANNULE);
        long nbBenevoles    = affectationRepository.countBenevolesDistinctsParEvenement(evenementId);
        double taux         = nbPlaces > 0 ? Math.round((double) nbConfirmes / nbPlaces * 1000.0) / 10.0 : 0.0;

        List<DashboardSnapshotResponse.MissionStat> statsParMission =
            missionRepository.findByEvenementId(evenementId).stream()
                .map(m -> missionStat(m))
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

    private DashboardSnapshotResponse.MissionStat missionStat(Mission mission) {
        long places    = mission.getCreneaux() != null
            ? mission.getCreneaux().stream().mapToLong(c -> c.getNbBenevolesRequis()).sum()
            : 0;
        long confirmes = affectationRepository.countParMissionEtStatut(mission.getId(), StatutAffectation.CONFIRME);
        double taux    = places > 0 ? Math.round((double) confirmes / places * 1000.0) / 10.0 : 0.0;

        DashboardSnapshotResponse.MissionStat stat = new DashboardSnapshotResponse.MissionStat();
        stat.setMissionId(mission.getId());
        stat.setMissionNom(mission.getNom());
        stat.setCategorie(mission.getCategorie().name());
        stat.setNbPlacesRequises(places);
        stat.setNbConfirmes(confirmes);
        stat.setTauxRemplissage(taux);
        return stat;
    }

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
