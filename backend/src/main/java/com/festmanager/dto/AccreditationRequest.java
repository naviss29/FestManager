package com.festmanager.dto;

import com.festmanager.entity.enums.TypeAccreditation;
import com.festmanager.entity.enums.ZoneAcces;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public class AccreditationRequest {

    @NotNull(message = "L'identifiant du bénévole est obligatoire")
    private UUID benevoleId;

    @NotNull(message = "L'identifiant de l'événement est obligatoire")
    private UUID evenementId;

    @NotNull(message = "Le type d'accréditation est obligatoire")
    private TypeAccreditation type;

    private Set<ZoneAcces> zonesAcces;

    private LocalDate dateDebutValidite;

    private LocalDate dateFinValidite;

    // Getters / Setters

    public UUID getBenevoleId() { return benevoleId; }
    public void setBenevoleId(UUID benevoleId) { this.benevoleId = benevoleId; }

    public UUID getEvenementId() { return evenementId; }
    public void setEvenementId(UUID evenementId) { this.evenementId = evenementId; }

    public TypeAccreditation getType() { return type; }
    public void setType(TypeAccreditation type) { this.type = type; }

    public Set<ZoneAcces> getZonesAcces() { return zonesAcces; }
    public void setZonesAcces(Set<ZoneAcces> zonesAcces) { this.zonesAcces = zonesAcces; }

    public LocalDate getDateDebutValidite() { return dateDebutValidite; }
    public void setDateDebutValidite(LocalDate dateDebutValidite) { this.dateDebutValidite = dateDebutValidite; }

    public LocalDate getDateFinValidite() { return dateFinValidite; }
    public void setDateFinValidite(LocalDate dateFinValidite) { this.dateFinValidite = dateFinValidite; }
}
