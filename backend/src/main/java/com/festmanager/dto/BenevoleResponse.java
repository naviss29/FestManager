package com.festmanager.dto;

import com.festmanager.entity.enums.StatutCompteBenevole;
import com.festmanager.entity.enums.TailleTshirt;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BenevoleResponse {

    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String competences;
    private TailleTshirt tailleTshirt;
    private LocalDate dateNaissance;
    private String disponibilites;
    private StatutCompteBenevole statutCompte;
    private String photoUrl;
    private Boolean consentementRgpd;
    private LocalDateTime dateConsentement;
    private String versionCgu;
    private LocalDateTime createdAt;
}
