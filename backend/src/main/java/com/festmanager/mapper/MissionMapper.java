package com.festmanager.mapper;

import com.festmanager.dto.MissionRequest;
import com.festmanager.dto.MissionResponse;
import com.festmanager.entity.Evenement;
import com.festmanager.entity.Mission;
import com.festmanager.entity.Organisation;
import org.springframework.stereotype.Component;

@Component
public class MissionMapper {

    public Mission toEntity(MissionRequest request, Evenement evenement, Organisation organisation) {
        return Mission.builder()
                .evenement(evenement)
                .organisation(organisation)
                .nom(request.getNom())
                .description(request.getDescription())
                .lieu(request.getLieu())
                .materielRequis(request.getMaterielRequis())
                .categorie(request.getCategorie())
                .nbBenevolesRequis(request.getNbBenevolesRequis())
                .multiAffectationAutorisee(Boolean.TRUE.equals(request.getMultiAffectationAutorisee()))
                .gereeParOrganisation(Boolean.TRUE.equals(request.getGereeParOrganisation()))
                .build();
    }

    public MissionResponse toResponse(Mission mission) {
        MissionResponse response = new MissionResponse();
        response.setId(mission.getId());
        response.setEvenementId(mission.getEvenement().getId());
        response.setEvenementNom(mission.getEvenement().getNom());
        response.setNom(mission.getNom());
        response.setDescription(mission.getDescription());
        response.setLieu(mission.getLieu());
        response.setMaterielRequis(mission.getMaterielRequis());
        response.setCategorie(mission.getCategorie());
        response.setNbBenevolesRequis(mission.getNbBenevolesRequis());
        response.setMultiAffectationAutorisee(mission.getMultiAffectationAutorisee());
        response.setGereeParOrganisation(mission.getGereeParOrganisation());
        response.setCreatedAt(mission.getCreatedAt());
        if (mission.getOrganisation() != null) {
            response.setOrganisationId(mission.getOrganisation().getId());
            response.setOrganisationNom(mission.getOrganisation().getNom());
        }
        return response;
    }
}
