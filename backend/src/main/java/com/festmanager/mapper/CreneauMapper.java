package com.festmanager.mapper;

import com.festmanager.dto.CreneauRequest;
import com.festmanager.dto.CreneauResponse;
import com.festmanager.entity.Creneau;
import com.festmanager.entity.Mission;
import com.festmanager.repository.AffectationRepository;
import com.festmanager.entity.enums.StatutAffectation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreneauMapper {

    private final AffectationRepository affectationRepository;

    public Creneau toEntity(CreneauRequest request, Mission mission) {
        return Creneau.builder()
                .mission(mission)
                .debut(request.getDebut())
                .fin(request.getFin())
                .nbBenevolesRequis(request.getNbBenevolesRequis())
                .build();
    }

    /**
     * Conversion simple (1 créneau) : exécute une requête COUNT individuelle.
     * À utiliser uniquement pour la lecture d'un créneau unique (obtenirCreneau, creerCreneau, modifierCreneau).
     * Pour une liste de créneaux, préférer toResponse(creneau, nbAffectes) avec un batch count
     * préalable dans le service afin d'éviter le problème N+1.
     */
    public CreneauResponse toResponse(Creneau creneau) {
        int count = affectationRepository.countByCreneauIdAndStatut(creneau.getId(), StatutAffectation.CONFIRME);
        return toResponse(creneau, count);
    }

    /**
     * Conversion avec le nombre d'affectés fourni en paramètre.
     * Cette surcharge est utilisée par CreneauService.listerCreneaux() qui calcule
     * les counts en une seule requête groupée pour tous les créneaux, puis les passe ici.
     * Cela évite d'exécuter une requête COUNT par créneau dans la boucle (N+1).
     */
    public CreneauResponse toResponse(Creneau creneau, int nbAffectes) {
        CreneauResponse response = new CreneauResponse();
        response.setId(creneau.getId());
        response.setMissionId(creneau.getMission().getId());
        response.setMissionNom(creneau.getMission().getNom());
        response.setDebut(creneau.getDebut());
        response.setFin(creneau.getFin());
        response.setNbBenevolesRequis(creneau.getNbBenevolesRequis());
        response.setNbBenevolesAffectes(nbAffectes);
        return response;
    }
}
