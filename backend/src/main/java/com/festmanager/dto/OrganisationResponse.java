package com.festmanager.dto;

import com.festmanager.entity.enums.TypeOrganisation;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OrganisationResponse {

    private UUID id;
    private String nom;
    private TypeOrganisation type;
    private String siret;
    private String emailContact;
    private String telephoneContact;
    private String adresse;
    private LocalDateTime createdAt;
}
