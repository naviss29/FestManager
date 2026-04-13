package com.festmanager.dto;

import com.festmanager.entity.enums.TypeOrganisation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrganisationRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255)
    private String nom;

    @NotNull(message = "Le type est obligatoire")
    private TypeOrganisation type;

    @Size(max = 14, message = "Le SIRET doit contenir 14 caractères maximum")
    private String siret;

    @NotBlank(message = "L'email de contact est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 255)
    private String emailContact;

    @Size(max = 20)
    private String telephoneContact;

    private String adresse;
}
