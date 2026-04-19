package com.festmanager.entity;

import com.festmanager.entity.enums.StatutCompteBenevole;
import com.festmanager.entity.enums.TailleTshirt;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité centrale RGPD : contient toutes les données personnelles des bénévoles.
 * Les champs personnels (nom, prénom, email, téléphone) sont anonymisés
 * lors d'un effacement (statut ANONYMISE).
 */
@Entity
@Table(name = "benevole")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Benevole {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 20)
    private String telephone;

    @Column(columnDefinition = "TEXT")
    private String competences;

    @Enumerated(EnumType.STRING)
    @Column(name = "taille_tshirt")
    private TailleTshirt tailleTshirt;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    // Préférences de disponibilité avant affectation (jours/créneaux souhaités)
    @Column(columnDefinition = "TEXT")
    private String disponibilites;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_compte", nullable = false)
    @Builder.Default
    private StatutCompteBenevole statutCompte = StatutCompteBenevole.INVITE;

    // --- RGPD ---

    @Column(name = "consentement_rgpd", nullable = false)
    private Boolean consentementRgpd;

    @Column(name = "date_consentement", nullable = false)
    private LocalDateTime dateConsentement;

    @Column(name = "version_cgu", nullable = false, length = 10)
    private String versionCgu;

    @Column(name = "date_anonymisation")
    private LocalDateTime dateAnonymisation;

    /** URL relative de la photo de profil (ex: /api/fichiers/benevoles/id.jpg). Null si aucune photo. */
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
