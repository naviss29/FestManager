package com.festmanager.service;

import com.festmanager.dto.CreneauRequest;
import com.festmanager.dto.CreneauResponse;
import com.festmanager.entity.Creneau;
import com.festmanager.entity.Mission;
import com.festmanager.entity.enums.StatutAffectation;
import com.festmanager.mapper.CreneauMapper;
import com.festmanager.repository.AffectationRepository;
import com.festmanager.repository.CreneauRepository;
import com.festmanager.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreneauService {

    private final CreneauRepository creneauRepository;
    private final MissionRepository missionRepository;
    private final AffectationRepository affectationRepository;
    private final CreneauMapper creneauMapper;

    /**
     * Liste tous les créneaux d'une mission avec leur nombre de bénévoles confirmés.
     *
     * Stratégie anti-N+1 :
     *   1. On charge tous les créneaux de la mission en une requête.
     *   2. On récupère les counts d'affectations confirmées pour TOUS ces créneaux
     *      en une seule requête SQL GROUP BY (au lieu d'une COUNT par créneau).
     *   3. On assemble le tout côté Java via une Map<creneauId, count>.
     *
     * Résultat : 2 requêtes SQL au total, quelle que soit la taille de la mission.
     */
    @Transactional(readOnly = true)
    public List<CreneauResponse> listerCreneaux(UUID missionId) {
        verifierMissionExiste(missionId);

        // Étape 1 : récupérer tous les créneaux de la mission
        List<Creneau> creneaux = creneauRepository.findByMissionId(missionId);
        List<UUID> ids = creneaux.stream().map(Creneau::getId).toList();

        // Étape 2 : compter les affectations CONFIRME pour tous les créneaux en une seule requête groupée.
        // countGroupedByCreneauIds retourne List<Object[]> avec [0]=creneauId, [1]=count.
        Map<UUID, Integer> counts = affectationRepository
                .countGroupedByCreneauIds(ids, StatutAffectation.CONFIRME)
                .stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> ((Number) row[1]).intValue()));

        // Étape 3 : construire les réponses en injectant le count pré-calculé (pas de requête supplémentaire)
        return creneaux.stream()
                .map(c -> creneauMapper.toResponse(c, counts.getOrDefault(c.getId(), 0)))
                .toList();
    }

    @Transactional(readOnly = true)
    public CreneauResponse obtenirCreneau(UUID id) {
        // Lecture unitaire : le mapper fait sa propre requête COUNT (acceptable pour 1 créneau)
        return creneauMapper.toResponse(trouverParId(id));
    }

    @Transactional
    public CreneauResponse creerCreneau(UUID missionId, CreneauRequest request) {
        validerDates(request);
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mission introuvable"));
        Creneau creneau = creneauMapper.toEntity(request, mission);
        return creneauMapper.toResponse(creneauRepository.save(creneau));
    }

    @Transactional
    public CreneauResponse modifierCreneau(UUID id, CreneauRequest request) {
        validerDates(request);
        Creneau creneau = trouverParId(id);
        creneau.setDebut(request.getDebut());
        creneau.setFin(request.getFin());
        creneau.setNbBenevolesRequis(request.getNbBenevolesRequis());
        return creneauMapper.toResponse(creneauRepository.save(creneau));
    }

    @Transactional
    public void supprimerCreneau(UUID id) {
        creneauRepository.delete(trouverParId(id));
    }

    private Creneau trouverParId(UUID id) {
        return creneauRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Créneau introuvable"));
    }

    private void verifierMissionExiste(UUID missionId) {
        if (!missionRepository.existsById(missionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mission introuvable");
        }
    }

    private void validerDates(CreneauRequest request) {
        if (!request.getFin().isAfter(request.getDebut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fin du créneau doit être après son début");
        }
    }
}
