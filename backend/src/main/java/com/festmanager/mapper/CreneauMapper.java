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

    public CreneauResponse toResponse(Creneau creneau) {
        CreneauResponse response = new CreneauResponse();
        response.setId(creneau.getId());
        response.setMissionId(creneau.getMission().getId());
        response.setMissionNom(creneau.getMission().getNom());
        response.setDebut(creneau.getDebut());
        response.setFin(creneau.getFin());
        response.setNbBenevolesRequis(creneau.getNbBenevolesRequis());
        response.setNbBenevolesAffectes(
                affectationRepository.countByCreneauIdAndStatut(creneau.getId(), StatutAffectation.CONFIRME)
        );
        return response;
    }
}
