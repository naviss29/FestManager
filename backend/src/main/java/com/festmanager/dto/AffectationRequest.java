package com.festmanager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AffectationRequest {

    @NotNull(message = "L'identifiant du bénévole est obligatoire")
    private UUID benevoleId;

    @NotNull(message = "L'identifiant du créneau est obligatoire")
    private UUID creneauId;

    private String commentaire;
}
