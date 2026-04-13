package com.festmanager.mapper;

import com.festmanager.dto.AffectationResponse;
import com.festmanager.entity.Affectation;
import org.springframework.stereotype.Component;

@Component
public class AffectationMapper {

    public AffectationResponse toResponse(Affectation affectation) {
        AffectationResponse response = new AffectationResponse();
        response.setId(affectation.getId());

        response.setBenevoleId(affectation.getBenevole().getId());
        response.setBenevoleNom(affectation.getBenevole().getNom());
        response.setBenevolePrenom(affectation.getBenevole().getPrenom());

        response.setCreneauId(affectation.getCreneau().getId());
        response.setCreneauDebut(affectation.getCreneau().getDebut());
        response.setCreneauFin(affectation.getCreneau().getFin());

        response.setMissionId(affectation.getCreneau().getMission().getId());
        response.setMissionNom(affectation.getCreneau().getMission().getNom());

        response.setEvenementId(affectation.getCreneau().getMission().getEvenement().getId());
        response.setEvenementNom(affectation.getCreneau().getMission().getEvenement().getNom());

        response.setStatut(affectation.getStatut());
        response.setCommentaire(affectation.getCommentaire());
        response.setCreatedAt(affectation.getCreatedAt());
        return response;
    }
}
