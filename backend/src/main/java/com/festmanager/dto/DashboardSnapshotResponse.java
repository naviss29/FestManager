package com.festmanager.dto;

import java.util.List;
import java.util.UUID;

/**
 * Snapshot complet du dashboard pour un événement.
 * Chargé une fois à l'ouverture, puis mis à jour via WebSocket.
 */
public class DashboardSnapshotResponse {

    private UUID evenementId;
    private String evenementNom;

    // Compteurs globaux
    private long nbMissions;
    private long nbCreneaux;
    private long nbBenevolesEngages;   // bénévoles distincts avec au moins une affectation
    private long nbPlacesRequises;     // somme des nbBenevolesRequis de tous les créneaux
    private long nbConfirmes;
    private long nbEnAttente;
    private long nbRefuses;
    private long nbAnnules;

    // Taux de remplissage global (0–100)
    private double tauxRemplissage;

    // Détail par mission
    private List<MissionStat> missions;

    // --- Classe imbriquée ---

    public static class MissionStat {
        private UUID missionId;
        private String missionNom;
        private String categorie;
        private long nbPlacesRequises;
        private long nbConfirmes;
        private double tauxRemplissage;

        public UUID getMissionId() { return missionId; }
        public void setMissionId(UUID missionId) { this.missionId = missionId; }

        public String getMissionNom() { return missionNom; }
        public void setMissionNom(String missionNom) { this.missionNom = missionNom; }

        public String getCategorie() { return categorie; }
        public void setCategorie(String categorie) { this.categorie = categorie; }

        public long getNbPlacesRequises() { return nbPlacesRequises; }
        public void setNbPlacesRequises(long nbPlacesRequises) { this.nbPlacesRequises = nbPlacesRequises; }

        public long getNbConfirmes() { return nbConfirmes; }
        public void setNbConfirmes(long nbConfirmes) { this.nbConfirmes = nbConfirmes; }

        public double getTauxRemplissage() { return tauxRemplissage; }
        public void setTauxRemplissage(double tauxRemplissage) { this.tauxRemplissage = tauxRemplissage; }
    }

    // --- Getters / Setters ---

    public UUID getEvenementId() { return evenementId; }
    public void setEvenementId(UUID evenementId) { this.evenementId = evenementId; }

    public String getEvenementNom() { return evenementNom; }
    public void setEvenementNom(String evenementNom) { this.evenementNom = evenementNom; }

    public long getNbMissions() { return nbMissions; }
    public void setNbMissions(long nbMissions) { this.nbMissions = nbMissions; }

    public long getNbCreneaux() { return nbCreneaux; }
    public void setNbCreneaux(long nbCreneaux) { this.nbCreneaux = nbCreneaux; }

    public long getNbBenevolesEngages() { return nbBenevolesEngages; }
    public void setNbBenevolesEngages(long nbBenevolesEngages) { this.nbBenevolesEngages = nbBenevolesEngages; }

    public long getNbPlacesRequises() { return nbPlacesRequises; }
    public void setNbPlacesRequises(long nbPlacesRequises) { this.nbPlacesRequises = nbPlacesRequises; }

    public long getNbConfirmes() { return nbConfirmes; }
    public void setNbConfirmes(long nbConfirmes) { this.nbConfirmes = nbConfirmes; }

    public long getNbEnAttente() { return nbEnAttente; }
    public void setNbEnAttente(long nbEnAttente) { this.nbEnAttente = nbEnAttente; }

    public long getNbRefuses() { return nbRefuses; }
    public void setNbRefuses(long nbRefuses) { this.nbRefuses = nbRefuses; }

    public long getNbAnnules() { return nbAnnules; }
    public void setNbAnnules(long nbAnnules) { this.nbAnnules = nbAnnules; }

    public double getTauxRemplissage() { return tauxRemplissage; }
    public void setTauxRemplissage(double tauxRemplissage) { this.tauxRemplissage = tauxRemplissage; }

    public List<MissionStat> getMissions() { return missions; }
    public void setMissions(List<MissionStat> missions) { this.missions = missions; }
}
