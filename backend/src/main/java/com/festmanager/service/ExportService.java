package com.festmanager.service;

import com.festmanager.entity.Affectation;
import com.festmanager.entity.Evenement;
import com.festmanager.repository.AffectationRepository;
import com.festmanager.repository.EvenementRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final AffectationRepository affectationRepository;
    private final EvenementRepository evenementRepository;

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] EN_TETES_CSV = {
        "Événement", "Mission", "Catégorie", "Début créneau", "Fin créneau",
        "Prénom bénévole", "Nom bénévole", "Statut affectation", "Commentaire"
    };

    // -------------------------------------------------------------------------
    // CSV
    // -------------------------------------------------------------------------

    /**
     * Génère un fichier CSV UTF-8 de toutes les affectations d'un événement.
     * Une ligne = une affectation (bénévole × créneau).
     */
    public byte[] exporterCsv(UUID evenementId) {
        Evenement evenement = trouverEvenement(evenementId);
        List<Affectation> affectations = affectationRepository.findParEvenementPourExport(evenementId);

        try (ByteArrayOutputStream sortie = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(sortie, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL
                 .builder()
                 .setHeader(EN_TETES_CSV)
                 .setDelimiter(';')
                 .build())) {

            for (Affectation a : affectations) {
                printer.printRecord(
                    evenement.getNom(),
                    a.getCreneau().getMission().getNom(),
                    a.getCreneau().getMission().getCategorie().name(),
                    a.getCreneau().getDebut().format(FORMAT_DATE),
                    a.getCreneau().getFin().format(FORMAT_DATE),
                    a.getBenevole().getPrenom(),
                    a.getBenevole().getNom(),
                    a.getStatut().name(),
                    a.getCommentaire() != null ? a.getCommentaire() : ""
                );
            }
            printer.flush();
            return sortie.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la génération du CSV", e);
        }
    }

    // -------------------------------------------------------------------------
    // PDF
    // -------------------------------------------------------------------------

    /**
     * Génère un PDF de planning structuré par mission.
     * Chaque mission est un tableau avec ses créneaux et les bénévoles affectés.
     */
    public byte[] exporterPdf(UUID evenementId) {
        Evenement evenement = trouverEvenement(evenementId);
        List<Affectation> affectations = affectationRepository.findParEvenementPourExport(evenementId);

        ByteArrayOutputStream sortie = new ByteArrayOutputStream();

        try {
            Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(doc, sortie);
            doc.open();

            // --- En-tête document ---
            Font fontTitre   = new Font(Font.HELVETICA, 18, Font.BOLD, Color.decode("#1a237e"));
            Font fontSousTitre = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.DARK_GRAY);
            Font fontMission = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
            Font fontEntete  = new Font(Font.HELVETICA, 9, Font.BOLD, Color.decode("#37474f"));
            Font fontCell    = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);

            Paragraph titre = new Paragraph("Planning — " + evenement.getNom(), fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            doc.add(titre);

            String dates = evenement.getDateDebut() + " → " + evenement.getDateFin()
                + "  ·  " + evenement.getLieu();
            Paragraph sousTitre = new Paragraph(dates, fontSousTitre);
            sousTitre.setAlignment(Element.ALIGN_CENTER);
            sousTitre.setSpacingAfter(20);
            doc.add(sousTitre);

            // --- Regroupement par mission ---
            String missionCourante = null;
            PdfPTable tableau = null;

            for (Affectation a : affectations) {
                String nomMission = a.getCreneau().getMission().getNom();

                // Nouveau groupe mission
                if (!nomMission.equals(missionCourante)) {
                    if (tableau != null) {
                        doc.add(tableau);
                        doc.add(new Paragraph(" "));
                    }

                    // Bandeau mission
                    PdfPTable bandeau = new PdfPTable(1);
                    bandeau.setWidthPercentage(100);
                    PdfPCell cellMission = new PdfPCell(new Phrase(
                        nomMission + "  ·  " + a.getCreneau().getMission().getCategorie().name(),
                        fontMission
                    ));
                    cellMission.setBackgroundColor(Color.decode("#283593"));
                    cellMission.setPadding(6);
                    cellMission.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                    bandeau.addCell(cellMission);
                    doc.add(bandeau);

                    // En-têtes colonnes
                    tableau = new PdfPTable(new float[]{3, 3, 3, 2, 3});
                    tableau.setWidthPercentage(100);
                    tableau.setSpacingBefore(0);
                    ajouterEntete(tableau, "Début", fontEntete);
                    ajouterEntete(tableau, "Fin", fontEntete);
                    ajouterEntete(tableau, "Bénévole", fontEntete);
                    ajouterEntete(tableau, "Statut", fontEntete);
                    ajouterEntete(tableau, "Commentaire", fontEntete);

                    missionCourante = nomMission;
                }

                // Ligne affectation
                Color fond = affectations.indexOf(a) % 2 == 0 ? Color.WHITE : Color.decode("#f5f5f5");
                ajouterCell(tableau, a.getCreneau().getDebut().format(FORMAT_DATE), fontCell, fond);
                ajouterCell(tableau, a.getCreneau().getFin().format(FORMAT_DATE), fontCell, fond);
                ajouterCell(tableau, a.getBenevole().getPrenom() + " " + a.getBenevole().getNom(), fontCell, fond);
                ajouterCell(tableau, a.getStatut().name(), fontCell, fond);
                ajouterCell(tableau, a.getCommentaire() != null ? a.getCommentaire() : "", fontCell, fond);
            }

            if (tableau != null) doc.add(tableau);

            if (affectations.isEmpty()) {
                doc.add(new Paragraph("Aucune affectation pour cet événement.", fontSousTitre));
            }

            doc.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }

        return sortie.toByteArray();
    }

    // -------------------------------------------------------------------------
    // Méthodes privées
    // -------------------------------------------------------------------------

    private Evenement trouverEvenement(UUID evenementId) {
        return evenementRepository.findById(evenementId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable"));
    }

    private void ajouterEntete(PdfPTable table, String texte, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, font));
        cell.setBackgroundColor(Color.decode("#eceff1"));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void ajouterCell(PdfPTable table, String texte, Font font, Color fond) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, font));
        cell.setBackgroundColor(fond);
        cell.setPadding(4);
        cell.setBorderColor(Color.decode("#e0e0e0"));
        table.addCell(cell);
    }
}
