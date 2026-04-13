package com.festmanager.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreneauRequest {

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime debut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime fin;

    @NotNull(message = "Le nombre de bénévoles requis est obligatoire")
    @Min(value = 1, message = "Il faut au moins 1 bénévole")
    private Integer nbBenevolesRequis;
}
