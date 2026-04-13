package com.festmanager.entity;

import com.festmanager.entity.enums.StatutAffectation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Table pivot entre un bénévole et un créneau de mission.
 * Lors de la création, le service vérifie l'absence de chevauchement horaire
 * pour ce bénévole sur le même événement.
 */
@Entity
@Table(
    name = "affectation",
    uniqueConstraints = @UniqueConstraint(columnNames = {"benevole_id", "creneau_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Affectation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benevole_id", nullable = false)
    private Benevole benevole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creneau_id", nullable = false)
    private Creneau creneau;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutAffectation statut = StatutAffectation.EN_ATTENTE;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
