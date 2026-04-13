package com.festmanager.service;

import com.festmanager.entity.*;
import com.festmanager.entity.enums.CategorieMission;
import com.festmanager.entity.enums.StatutAffectation;
import com.festmanager.repository.AffectationRepository;
import com.festmanager.repository.EvenementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExportService")
class ExportServiceTest {

    @Mock AffectationRepository affectationRepository;
    @Mock EvenementRepository evenementRepository;

    @InjectMocks ExportService exportService;

    @Test
    @DisplayName("exporterCsv — produit du contenu CSV non vide avec les en-têtes")
    void exporterCsv_produitCsvAvecEntetes() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(evenementRepository.findById(eventId)).thenReturn(Optional.of(evenement(eventId)));
        when(affectationRepository.findParEvenementPourExport(eventId)).thenReturn(List.of(affectation()));

        byte[] csv = exportService.exporterCsv(eventId);
        String contenu = new String(csv, "UTF-8");

        assertThat(contenu).contains("Événement");
        assertThat(contenu).contains("Mission");
        assertThat(contenu).contains("Accueil entrée");
        assertThat(contenu).contains("Dupont");
    }

    @Test
    @DisplayName("exporterCsv — produit un CSV vide (juste en-têtes) s'il n'y a pas d'affectations")
    void exporterCsv_sansAffectations_produitsEntetes() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(evenementRepository.findById(eventId)).thenReturn(Optional.of(evenement(eventId)));
        when(affectationRepository.findParEvenementPourExport(eventId)).thenReturn(List.of());

        byte[] csv = exportService.exporterCsv(eventId);
        String contenu = new String(csv, "UTF-8");

        assertThat(contenu).contains("Événement");
        assertThat(contenu.lines().count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("exporterPdf — produit un PDF (signature %PDF)")
    void exporterPdf_produitPdf() {
        UUID eventId = UUID.randomUUID();
        when(evenementRepository.findById(eventId)).thenReturn(Optional.of(evenement(eventId)));
        when(affectationRepository.findParEvenementPourExport(eventId)).thenReturn(List.of(affectation()));

        byte[] pdf = exportService.exporterPdf(eventId);

        assertThat(pdf).isNotEmpty();
        // Signature PDF : %PDF
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("exporterCsv — lève NOT_FOUND si l'événement est introuvable")
    void exporterCsv_leveNotFoundSiEvenementMissing() {
        UUID eventId = UUID.randomUUID();
        when(evenementRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exportService.exporterCsv(eventId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("introuvable");
    }

    // --- Helpers ---

    private Evenement evenement(UUID id) {
        Evenement e = new Evenement();
        e.setId(id);
        e.setNom("HellFest 2026");
        e.setLieu("Clisson");
        e.setDateDebut(LocalDate.of(2026, 6, 19));
        e.setDateFin(LocalDate.of(2026, 6, 22));
        return e;
    }

    private Affectation affectation() {
        Evenement evenement = evenement(UUID.randomUUID());

        Mission mission = new Mission();
        mission.setId(UUID.randomUUID());
        mission.setNom("Accueil entrée");
        mission.setCategorie(CategorieMission.ACCUEIL);
        mission.setLieu("Porte A");
        mission.setEvenement(evenement);

        Creneau creneau = new Creneau();
        creneau.setId(UUID.randomUUID());
        creneau.setMission(mission);
        creneau.setDebut(LocalDateTime.of(2026, 6, 20, 8, 0));
        creneau.setFin(LocalDateTime.of(2026, 6, 20, 14, 0));
        creneau.setNbBenevolesRequis(5);

        Benevole b = new Benevole();
        b.setId(UUID.randomUUID());
        b.setNom("Dupont");
        b.setPrenom("Marie");
        b.setEmail("marie.dupont@test.fr");

        Affectation a = new Affectation();
        a.setId(UUID.randomUUID());
        a.setBenevole(b);
        a.setCreneau(creneau);
        a.setStatut(StatutAffectation.CONFIRME);
        return a;
    }
}
