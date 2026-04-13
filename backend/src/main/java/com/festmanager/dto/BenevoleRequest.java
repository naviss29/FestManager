package com.festmanager.dto;

import com.festmanager.entity.enums.TailleTshirt;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BenevoleRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String telephone;

    private String competences;

    private TailleTshirt tailleTshirt;

    private LocalDate dateNaissance;

    private String disponibilites;

    @NotNull(message = "Le consentement RGPD est obligatoire")
    @AssertTrue(message = "Le consentement RGPD doit être accepté")
    private Boolean consentementRgpd;
}
