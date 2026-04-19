package com.festmanager.mapper;

import com.festmanager.dto.EvenementRequest;
import com.festmanager.dto.EvenementResponse;
import com.festmanager.entity.Evenement;
import com.festmanager.entity.Utilisateur;
import org.springframework.stereotype.Component;

@Component
public class EvenementMapper {

    public Evenement toEntity(EvenementRequest request, Utilisateur organisateur) {
        return Evenement.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .lieu(request.getLieu())
                .organisateur(organisateur)
                .build();
    }

    public EvenementResponse toResponse(Evenement evenement) {
        EvenementResponse response = new EvenementResponse();
        response.setId(evenement.getId());
        response.setNom(evenement.getNom());
        response.setDescription(evenement.getDescription());
        response.setDateDebut(evenement.getDateDebut());
        response.setDateFin(evenement.getDateFin());
        response.setLieu(evenement.getLieu());
        response.setStatut(evenement.getStatut());
        response.setBanniereUrl(evenement.getBanniereUrl());
        response.setOrganisateurId(evenement.getOrganisateur().getId());
        response.setOrganisateurEmail(evenement.getOrganisateur().getEmail());
        response.setCreatedAt(evenement.getCreatedAt());
        return response;
    }
}
