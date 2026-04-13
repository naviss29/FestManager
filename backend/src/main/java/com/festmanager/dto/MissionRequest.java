package com.festmanager.dto;

import com.festmanager.entity.enums.CategorieMission;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class MissionRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255)
    private String nom;

    private String description;

    @Size(max = 255)
    private String lieu;

    private String materielRequis;

    @NotNull(message = "La catégorie est obligatoire")
    private CategorieMission categorie;

    @NotNull(message = "Le nombre de bénévoles requis est obligatoire")
    @Min(value = 1, message = "Il faut au moins 1 bénévole")
    private Integer nbBenevolesRequis;

    private Boolean multiAffectationAutorisee = false;

    private Boolean gereeParOrganisation = false;

    // Renseigné uniquement si gereeParOrganisation = true
    private UUID organisationId;
}
