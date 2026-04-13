package com.festmanager.service;

import com.festmanager.dto.AffectationRequest;
import com.festmanager.dto.AffectationResponse;
import com.festmanager.entity.Affectation;
import com.festmanager.entity.Benevole;
import com.festmanager.entity.Creneau;
import com.festmanager.entity.enums.StatutAffectation;
import com.festmanager.entity.enums.StatutCompteBenevole;
import com.festmanager.mapper.AffectationMapper;
import com.festmanager.repository.AffectationRepository;
import com.festmanager.repository.BenevoleRepository;
import com.festmanager.repository.CreneauRepository;
import com.festmanager.websocket.DashboardEvent;
import com.festmanager.websocket.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AffectationService {

    private final AffectationRepository affectationRepository;
    private final BenevoleRepository benevoleRepository;
    private final CreneauRepository creneauRepository;
    private final AffectationMapper affectationMapper;
    private final DashboardService dashboardService;

    // --- Lecture ---

    public List<AffectationResponse> listerParCreneau(UUID creneauId) {
        return affectationRepository.findByCreneauId(creneauId).stream()
                .map(affectationMapper::toResponse)
                .toList();
    }

    public List<AffectationResponse> listerParBenevole(UUID benevoleId) {
        return affectationRepository.findByBenevoleId(benevoleId).stream()
                .map(affectationMapper::toResponse)
                .toList();
    }

    public AffectationResponse obtenir(UUID id) {
        return affectationMapper.toResponse(trouverParId(id));
    }

    // --- Création avec contrôle des conflits ---

    @Transactional
    public AffectationResponse affecter(AffectationRequest request) {
        Benevole benevole = benevoleRepository.findById(request.getBenevoleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bénévole introuvable"));

        Creneau creneau = creneauRepository.findById(request.getCreneauId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Créneau introuvable"));

        // Vérification : bénévole non anonymisé
        if (benevole.getStatutCompte() == StatutCompteBenevole.ANONYMISE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible d'affecter un bénévole anonymisé");
        }

        // Vérification : affectation déjà existante
        if (affectationRepository.existsByBenevoleIdAndCreneauId(benevole.getId(), creneau.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce bénévole est déjà affecté à ce créneau");
        }

        // Vérification : capacité du créneau
        int nbConfirmes = affectationRepository.countByCreneauIdAndStatut(creneau.getId(), StatutAffectation.CONFIRME);
        if (nbConfirmes >= creneau.getNbBenevolesRequis()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce créneau est complet");
        }

        // Vérification des conflits horaires (sauf si multi-affectation autorisée sur la mission)
        if (!creneau.getMission().getMultiAffectationAutorisee()) {
            UUID evenementId = creneau.getMission().getEvenement().getId();
            List<Creneau> chevauchements = creneauRepository.findChevauchements(
                    evenementId, benevole.getId(), creneau.getDebut(), creneau.getFin()
            );
            if (!chevauchements.isEmpty()) {
                String details = chevauchements.stream()
                        .map(c -> c.getMission().getNom() + " (" + c.getDebut() + " → " + c.getFin() + ")")
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Conflit horaire avec : " + details);
            }
        }

        Affectation affectation = Affectation.builder()
                .benevole(benevole)
                .creneau(creneau)
                .statut(StatutAffectation.EN_ATTENTE)
                .commentaire(request.getCommentaire())
                .build();

        Affectation sauvegardee = affectationRepository.save(affectation);
        dashboardService.notifierAffectation(sauvegardee, DashboardEvent.TypeEvenement.AFFECTATION_CREEE);
        return affectationMapper.toResponse(sauvegardee);
    }

    // --- Changement de statut ---

    @Transactional
    public AffectationResponse changerStatut(UUID id, StatutAffectation nouveauStatut) {
        Affectation affectation = trouverParId(id);

        // On ne peut pas passer à CONFIRME si le créneau est déjà complet
        if (nouveauStatut == StatutAffectation.CONFIRME
                && affectation.getStatut() != StatutAffectation.CONFIRME) {
            int nbConfirmes = affectationRepository.countByCreneauIdAndStatut(
                    affectation.getCreneau().getId(), StatutAffectation.CONFIRME);
            if (nbConfirmes >= affectation.getCreneau().getNbBenevolesRequis()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Impossible de confirmer : le créneau est complet");
            }
        }

        affectation.setStatut(nouveauStatut);
        Affectation sauvegardee = affectationRepository.save(affectation);
        dashboardService.notifierAffectation(sauvegardee, DashboardEvent.TypeEvenement.AFFECTATION_MODIFIEE);
        return affectationMapper.toResponse(sauvegardee);
    }

    // --- Suppression ---

    @Transactional
    public void supprimer(UUID id) {
        Affectation affectation = trouverParId(id);
        dashboardService.notifierAffectation(affectation, DashboardEvent.TypeEvenement.AFFECTATION_SUPPRIMEE);
        affectationRepository.delete(affectation);
    }

    // --- Méthodes privées ---

    private Affectation trouverParId(UUID id) {
        return affectationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Affectation introuvable"));
    }
}
