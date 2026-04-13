package com.festmanager.dto;

import com.festmanager.entity.enums.StatutAffectation;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AffectationResponse {

    private UUID id;
    private UUID benevoleId;
    private String benevoleNom;
    private String benevolePrenom;
    private UUID creneauId;
    private LocalDateTime creneauDebut;
    private LocalDateTime creneauFin;
    private UUID missionId;
    private String missionNom;
    private UUID evenementId;
    private String evenementNom;
    private StatutAffectation statut;
    private String commentaire;
    private LocalDateTime createdAt;
}
