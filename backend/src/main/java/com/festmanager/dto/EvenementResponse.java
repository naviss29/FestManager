package com.festmanager.dto;

import com.festmanager.entity.enums.StatutEvenement;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EvenementResponse {

    private UUID id;
    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String lieu;
    private StatutEvenement statut;
    private UUID organisateurId;
    private String organisateurEmail;
    private LocalDateTime createdAt;
}
