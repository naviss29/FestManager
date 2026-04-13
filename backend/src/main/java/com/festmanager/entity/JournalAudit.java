package com.festmanager.entity;

import com.festmanager.entity.enums.ActionAudit;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Journal d'audit RGPD : trace tous les accès et modifications
 * sur les données personnelles des bénévoles.
 */
@Entity
@Table(name = "journal_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionAudit action;

    // Nom de l'entité accédée (ex : "BENEVOLE")
    @Column(name = "entite_cible", nullable = false, length = 50)
    private String entiteCible;

    // UUID de la ressource accédée
    @Column(name = "entite_id", nullable = false)
    private UUID entiteId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Détail de l'opération (ex : champs modifiés)
    @Column(columnDefinition = "TEXT")
    private String detail;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
