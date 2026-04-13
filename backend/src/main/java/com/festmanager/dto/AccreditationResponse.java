package com.festmanager.dto;

import com.festmanager.entity.enums.TypeAccreditation;
import com.festmanager.entity.enums.ZoneAcces;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class AccreditationResponse {

    private UUID id;
    private UUID benevoleId;
    private String benevoleNom;
    private String benevolePrenom;
    private UUID evenementId;
    private String evenementNom;
    private TypeAccreditation type;
    private Set<ZoneAcces> zonesAcces;
    private LocalDate dateDebutValidite;
    private LocalDate dateFinValidite;
    private String codeQr;
    // Image QR encodée en base64 (PNG 300x300) — utilisée directement en <img src>
    private String qrBase64;
    private Boolean valide;
    private LocalDateTime dateEmission;

    // Getters / Setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getBenevoleId() { return benevoleId; }
    public void setBenevoleId(UUID benevoleId) { this.benevoleId = benevoleId; }

    public String getBenevoleNom() { return benevoleNom; }
    public void setBenevoleNom(String benevoleNom) { this.benevoleNom = benevoleNom; }

    public String getBenevolePrenom() { return benevolePrenom; }
    public void setBenevolePrenom(String benevolePrenom) { this.benevolePrenom = benevolePrenom; }

    public UUID getEvenementId() { return evenementId; }
    public void setEvenementId(UUID evenementId) { this.evenementId = evenementId; }

    public String getEvenementNom() { return evenementNom; }
    public void setEvenementNom(String evenementNom) { this.evenementNom = evenementNom; }

    public TypeAccreditation getType() { return type; }
    public void setType(TypeAccreditation type) { this.type = type; }

    public Set<ZoneAcces> getZonesAcces() { return zonesAcces; }
    public void setZonesAcces(Set<ZoneAcces> zonesAcces) { this.zonesAcces = zonesAcces; }

    public LocalDate getDateDebutValidite() { return dateDebutValidite; }
    public void setDateDebutValidite(LocalDate dateDebutValidite) { this.dateDebutValidite = dateDebutValidite; }

    public LocalDate getDateFinValidite() { return dateFinValidite; }
    public void setDateFinValidite(LocalDate dateFinValidite) { this.dateFinValidite = dateFinValidite; }

    public String getCodeQr() { return codeQr; }
    public void setCodeQr(String codeQr) { this.codeQr = codeQr; }

    public String getQrBase64() { return qrBase64; }
    public void setQrBase64(String qrBase64) { this.qrBase64 = qrBase64; }

    public Boolean getValide() { return valide; }
    public void setValide(Boolean valide) { this.valide = valide; }

    public LocalDateTime getDateEmission() { return dateEmission; }
    public void setDateEmission(LocalDateTime dateEmission) { this.dateEmission = dateEmission; }
}
