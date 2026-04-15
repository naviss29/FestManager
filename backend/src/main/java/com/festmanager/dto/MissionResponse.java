package com.festmanager.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MissionResponse {

    private UUID id;
    private UUID evenementId;
    private String evenementNom;
    private UUID organisationId;
    private String organisationNom;
    private String nom;
    private String description;
    private String lieu;
    private String materielRequis;
    private String categorie;
    private Integer nbBenevolesRequis;
    private Boolean multiAffectationAutorisee;
    private Boolean gereeParOrganisation;
    private LocalDateTime createdAt;
}
