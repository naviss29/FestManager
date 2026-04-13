package com.festmanager.entity;

import com.festmanager.entity.enums.TypeAccreditation;
import com.festmanager.entity.enums.ZoneAcces;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
    name = "accreditation",
    uniqueConstraints = @UniqueConstraint(columnNames = {"benevole_id", "evenement_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Accreditation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benevole_id", nullable = false)
    private Benevole benevole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id", nullable = false)
    private Evenement evenement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAccreditation type;

    // Zones d'accès autorisées (GENERAL, SCENE, BACKSTAGE, VIP)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "accreditation_zones", joinColumns = @JoinColumn(name = "accreditation_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "zone")
    private Set<ZoneAcces> zonesAcces;

    @Column(name = "date_debut_validite")
    private LocalDate dateDebutValidite;

    @Column(name = "date_fin_validite")
    private LocalDate dateFinValidite;

    @Column(name = "code_qr", unique = true, length = 255)
    private String codeQr;

    @Column(nullable = false)
    @Builder.Default
    private Boolean valide = false;

    @Column(name = "date_emission")
    private LocalDateTime dateEmission;
}
