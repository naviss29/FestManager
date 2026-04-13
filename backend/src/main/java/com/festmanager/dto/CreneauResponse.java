package com.festmanager.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreneauResponse {

    private UUID id;
    private UUID missionId;
    private String missionNom;
    private LocalDateTime debut;
    private LocalDateTime fin;
    private Integer nbBenevolesRequis;
    // Nombre de bénévoles actuellement confirmés sur ce créneau
    private Integer nbBenevolesAffectes;
}
