package com.festmanager.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement temps réel envoyé au dashboard via WebSocket.
 * Notifie les clients d'un changement sur les affectations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEvent {

    public enum TypeEvenement {
        AFFECTATION_CREEE,
        AFFECTATION_MODIFIEE,
        AFFECTATION_SUPPRIMEE
    }

    private TypeEvenement type;
    private UUID evenementId;
    private UUID missionId;
    private UUID creneauId;
    private UUID benevoleId;
    private String missionNom;
    private int nbBenevolesAffectes;
    private int nbBenevolesRequis;
    private LocalDateTime timestamp;
}
