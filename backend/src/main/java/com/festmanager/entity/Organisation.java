package com.festmanager.entity;

import com.festmanager.entity.enums.TypeOrganisation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organisation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeOrganisation type;

    @Column(length = 14)
    private String siret;

    @Column(name = "email_contact", nullable = false, length = 255)
    private String emailContact;

    @Column(name = "telephone_contact", length = 20)
    private String telephoneContact;

    @Column(columnDefinition = "TEXT")
    private String adresse;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
