package com.festmanager.mapper;

import com.festmanager.dto.BenevoleRequest;
import com.festmanager.dto.BenevoleResponse;
import com.festmanager.entity.Benevole;
import com.festmanager.entity.enums.StatutCompteBenevole;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BenevoleMapper {

    private static final String VERSION_CGU_COURANTE = "1.0";

    public Benevole toEntity(BenevoleRequest request, StatutCompteBenevole statut) {
        return Benevole.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .competences(request.getCompetences())
                .tailleTshirt(request.getTailleTshirt())
                .dateNaissance(request.getDateNaissance())
                .disponibilites(request.getDisponibilites())
                .statutCompte(statut)
                .consentementRgpd(request.getConsentementRgpd())
                .dateConsentement(LocalDateTime.now())
                .versionCgu(VERSION_CGU_COURANTE)
                .build();
    }

    public BenevoleResponse toResponse(Benevole benevole) {
        BenevoleResponse response = new BenevoleResponse();
        response.setId(benevole.getId());
        response.setNom(benevole.getNom());
        response.setPrenom(benevole.getPrenom());
        response.setEmail(benevole.getEmail());
        response.setTelephone(benevole.getTelephone());
        response.setCompetences(benevole.getCompetences());
        response.setTailleTshirt(benevole.getTailleTshirt());
        response.setDateNaissance(benevole.getDateNaissance());
        response.setDisponibilites(benevole.getDisponibilites());
        response.setStatutCompte(benevole.getStatutCompte());
        response.setPhotoUrl(benevole.getPhotoUrl());
        response.setConsentementRgpd(benevole.getConsentementRgpd());
        response.setDateConsentement(benevole.getDateConsentement());
        response.setVersionCgu(benevole.getVersionCgu());
        response.setCreatedAt(benevole.getCreatedAt());
        return response;
    }
}
