package com.festmanager.entity;

import com.festmanager.entity.enums.CategorieMission;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id", nullable = false)
    private Evenement evenement;

    // Renseigné uniquement si geree_par_organisation = true
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    @Column(nullable = false, length = 255)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Localisation précise sur le site (ex : Scène A, Entrée Nord)
    @Column(length = 255)
    private String lieu;

    // Liste du matériel nécessaire pour la mission
    @Column(name = "materiel_requis", columnDefinition = "TEXT")
    private String materielRequis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieMission categorie;

    @Column(name = "nb_benevoles_requis", nullable = false)
    private Integer nbBenevolesRequis;

    // Si true, un bénévole peut être affecté à plusieurs créneaux simultanés sur cette mission
    @Column(name = "multi_affectation_autorisee", nullable = false)
    @Builder.Default
    private Boolean multiAffectationAutorisee = false;

    // Si true, la mission est entièrement sous la responsabilité de l'organisation prestataire
    @Column(name = "geree_par_organisation", nullable = false)
    @Builder.Default
    private Boolean gereeParOrganisation = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
