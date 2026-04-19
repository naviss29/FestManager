package com.festmanager.service;

import com.festmanager.entity.Accreditation;
import com.festmanager.entity.enums.ZoneAcces;
import com.festmanager.repository.AccreditationRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.UUID;

/**
 * Génère les badges PDF individuels et le ZIP multi-badges par événement.
 *
 * Format badge : A6 paysage (419 × 298 pt ≈ 148 × 105 mm).
 * Mise en page :
 *   - Bandeau supérieur navy : nom de l'événement + type d'accréditation
 *   - Corps gauche : prénom / nom / zones / validité
 *   - Corps droit  : QR code (130 × 130 pt)
 *   - Pied de page gris : branding + date d'émission
 */
@Service
@RequiredArgsConstructor
public class BadgeService {

    private final AccreditationRepository accreditationRepository;
    private final QrCodeService qrCodeService;
    private final FichierService fichierService;

    // ── Dimensions page (A6 paysage) ─────────────────────────────────────────
    private static final float PAGE_W  = 419f;
    private static final float PAGE_H  = 298f;
    private static final float MARGE   = 12f;

    // ── Zones verticales ──────────────────────────────────────────────────────
    // Header : de (PAGE_H - 35) à PAGE_H  — affiché en haut de la page
    private static final float HEADER_H = 35f;
    // Footer : de 0 à 25 pt
    private static final float FOOTER_H = 25f;

    // ── Séparateur vertical corps gauche / QR ────────────────────────────────
    private static final float SEP_X = 260f;

    // ── QR code ───────────────────────────────────────────────────────────────
    private static final float QR_SIZE = 130f;

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color NAVY   = Color.decode("#1a237e");
    private static final Color JAUNE  = Color.decode("#ffd740");
    private static final Color GRIS   = Color.decode("#f5f5f5");
    private static final Color BORDER = Color.decode("#e0e0e0");

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // -------------------------------------------------------------------------
    // API publique
    // -------------------------------------------------------------------------

    /**
     * Génère le badge PDF d'une accréditation.
     *
     * @param accreditationId UUID de l'accréditation
     * @return octets du fichier PDF
     * @throws ResponseStatusException 404 si l'accréditation est introuvable
     */
    @Transactional(readOnly = true)
    public byte[] genererBadge(UUID accreditationId) {
        Accreditation acc = accreditationRepository.findById(accreditationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Accréditation introuvable"));
        return construirePdfBadge(acc);
    }

    /**
     * Génère un ZIP contenant tous les badges PDF des accréditations d'un événement.
     * Chaque fichier est nommé : badge_NOM_PRENOM.pdf
     *
     * @param evenementId UUID de l'événement
     * @return octets du fichier ZIP
     */
    @Transactional(readOnly = true)
    public byte[] genererBadgesZip(UUID evenementId) {
        List<Accreditation> accreditations = accreditationRepository.findByEvenementId(evenementId);

        try (ByteArrayOutputStream sortieZip = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(sortieZip)) {

            for (Accreditation acc : accreditations) {
                byte[] pdfBadge = construirePdfBadge(acc);

                // Nom de fichier : caractères non-alphanumériques remplacés par "_"
                String nomFichier = String.format("badge_%s_%s.pdf",
                        sanitiser(acc.getBenevole().getNom()),
                        sanitiser(acc.getBenevole().getPrenom()));

                zip.putNextEntry(new ZipEntry(nomFichier));
                zip.write(pdfBadge);
                zip.closeEntry();
            }

            zip.finish();
            return sortieZip.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la génération du ZIP de badges", e);
        }
    }

    // -------------------------------------------------------------------------
    // Construction PDF
    // -------------------------------------------------------------------------

    /**
     * Construit le PDF du badge pour une accréditation donnée.
     * Utilise OpenPDF (fork de iText 2) déjà présent dans le projet.
     */
    private byte[] construirePdfBadge(Accreditation acc) {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();

        try {
            Document doc = new Document(new Rectangle(PAGE_W, PAGE_H), 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(doc, sortie);
            doc.open();

            PdfContentByte cb = writer.getDirectContent();

            dessinerBandeauHeader(cb, acc);
            dessinerCorpsTexte(cb, acc);
            dessinerQrCode(doc, cb, acc);
            dessinerPhotoProfil(doc, acc);
            dessinerFooter(cb, acc);
            dessinerBordure(cb);

            doc.close();
            return sortie.toByteArray();

        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Erreur lors de la génération du badge PDF", e);
        }
    }

    // ── Bandeau supérieur ─────────────────────────────────────────────────────

    private void dessinerBandeauHeader(PdfContentByte cb, Accreditation acc) throws DocumentException {
        float yBas = PAGE_H - HEADER_H;

        // Fond navy
        cb.setColorFill(NAVY);
        cb.rectangle(0, yBas, PAGE_W, HEADER_H);
        cb.fill();

        ColumnText ct = new ColumnText(cb);

        // Nom de l'événement (blanc, gauche)
        Font fontEvt = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
        ct.setSimpleColumn(MARGE, yBas, SEP_X, PAGE_H);
        ct.setAlignment(Element.ALIGN_LEFT);
        ct.setText(new Phrase(acc.getEvenement().getNom(), fontEvt));
        ct.go();

        // Type d'accréditation (jaune, droite)
        Font fontType = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, JAUNE);
        ct.setSimpleColumn(SEP_X, yBas, PAGE_W - MARGE, PAGE_H);
        ct.setAlignment(Element.ALIGN_RIGHT);
        ct.setText(new Phrase(acc.getType().name(), fontType));
        ct.go();
    }

    // ── Corps texte (partie gauche) ───────────────────────────────────────────

    private void dessinerCorpsTexte(PdfContentByte cb, Accreditation acc) throws DocumentException {
        float yCorpsHaut = PAGE_H - HEADER_H - 4f;
        float yCorpsBas  = FOOTER_H + 4f;

        ColumnText ct = new ColumnText(cb);

        Font fontPrenom = FontFactory.getFont(FontFactory.HELVETICA, 13, Color.DARK_GRAY);
        Font fontNom    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 19, NAVY);
        Font fontLabel  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.GRAY);
        Font fontValeur = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

        // Prénom (légèrement plus petit que le nom)
        ct.setSimpleColumn(MARGE, yCorpsHaut - 40, SEP_X - 4, yCorpsHaut);
        ct.setAlignment(Element.ALIGN_LEFT);
        ct.setText(new Phrase(acc.getBenevole().getPrenom().toUpperCase(), fontPrenom));
        ct.go();

        // Nom (grand, gras, couleur primaire)
        ct.setSimpleColumn(MARGE, yCorpsHaut - 78, SEP_X - 4, yCorpsHaut - 36);
        ct.setText(new Phrase(acc.getBenevole().getNom().toUpperCase(), fontNom));
        ct.go();

        // Zones d'accès
        String zones = acc.getZonesAcces().stream()
                .map(ZoneAcces::name)
                .collect(Collectors.joining("  ·  "));
        ct.setSimpleColumn(MARGE, yCorpsHaut - 120, SEP_X - 4, yCorpsHaut - 80);
        ct.setText(new Phrase("ZONES : " + zones, fontLabel));
        ct.go();

        // Validité
        ct.setSimpleColumn(MARGE, yCorpsHaut - 150, SEP_X - 4, yCorpsHaut - 118);
        ct.setText(new Phrase("VALIDITÉ : " + construireLibelleValidite(acc), fontLabel));
        ct.go();
    }

    // ── QR code (partie droite) ───────────────────────────────────────────────

    private void dessinerQrCode(Document doc, PdfContentByte cb, Accreditation acc)
            throws DocumentException, IOException {

        // Séparateur vertical entre texte et QR code
        cb.setColorStroke(BORDER);
        cb.setLineWidth(0.5f);
        cb.moveTo(SEP_X, FOOTER_H + 4f);
        cb.lineTo(SEP_X, PAGE_H - HEADER_H - 4f);
        cb.stroke();

        // Image QR code
        byte[] qrBytes = qrCodeService.genererQrCodeBytes(acc.getCodeQr());
        Image qrImage = Image.getInstance(qrBytes);
        qrImage.scaleToFit(QR_SIZE, QR_SIZE);

        // Centre horizontalement dans la colonne droite, aligne verticalement
        float qrX = SEP_X + ((PAGE_W - SEP_X - QR_SIZE) / 2f);
        float qrY = FOOTER_H + ((PAGE_H - FOOTER_H - HEADER_H - QR_SIZE) / 2f);
        qrImage.setAbsolutePosition(qrX, qrY);
        doc.add(qrImage);
    }

    // ── Photo de profil (bas-gauche, optionnelle) ─────────────────────────────

    /**
     * Ajoute la photo de profil du bénévole en bas à gauche du corps, si disponible.
     * En cas d'erreur de chargement, la photo est silencieusement ignorée.
     */
    private void dessinerPhotoProfil(Document doc, Accreditation acc)
            throws DocumentException, IOException {

        String photoUrl = acc.getBenevole().getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) return;

        // Extraire sousRep et nomFichier depuis l'URL : /api/fichiers/{sousRep}/{nomFichier}
        String[] parties = photoUrl.split("/");
        if (parties.length < 2) return;
        String sousRep   = parties[parties.length - 2];
        String nomFichier = parties[parties.length - 1];

        byte[] photoBytes;
        try {
            photoBytes = fichierService.charger(sousRep, nomFichier);
        } catch (Exception e) {
            // Photo absente ou illisible — on l'ignore, le badge reste valide
            return;
        }

        Image photo = Image.getInstance(photoBytes);
        float taille = 52f;
        photo.scaleToFit(taille, taille);
        // Positionné en bas à gauche du corps, au-dessus du footer
        photo.setAbsolutePosition(MARGE, FOOTER_H + 6f);
        doc.add(photo);
    }

    // ── Pied de page ──────────────────────────────────────────────────────────

    private void dessinerFooter(PdfContentByte cb, Accreditation acc) throws DocumentException {
        // Fond gris clair
        cb.setColorFill(GRIS);
        cb.rectangle(0, 0, PAGE_W, FOOTER_H);
        cb.fill();

        // Ligne de séparation footer / corps
        cb.setColorStroke(BORDER);
        cb.setLineWidth(0.5f);
        cb.moveTo(0, FOOTER_H);
        cb.lineTo(PAGE_W, FOOTER_H);
        cb.stroke();

        Font fontFooter = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.GRAY);

        ColumnText ct = new ColumnText(cb);

        // Branding gauche
        ct.setSimpleColumn(MARGE, 0, PAGE_W / 2f, FOOTER_H);
        ct.setAlignment(Element.ALIGN_LEFT);
        ct.setText(new Phrase("festmanager.app", fontFooter));
        ct.go();

        // Date d'émission droite
        String dateEmission = acc.getDateEmission() != null
                ? acc.getDateEmission().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : LocalDate.now().format(FMT_DATE);
        ct.setSimpleColumn(PAGE_W / 2f, 0, PAGE_W - MARGE, FOOTER_H);
        ct.setAlignment(Element.ALIGN_RIGHT);
        ct.setText(new Phrase("Émis le " + dateEmission, fontFooter));
        ct.go();
    }

    // ── Bordure extérieure ────────────────────────────────────────────────────

    private void dessinerBordure(PdfContentByte cb) {
        cb.setColorStroke(NAVY);
        cb.setLineWidth(2f);
        cb.rectangle(1f, 1f, PAGE_W - 2f, PAGE_H - 2f);
        cb.stroke();
    }

    // -------------------------------------------------------------------------
    // Utilitaires
    // -------------------------------------------------------------------------

    /** Libellé de validité selon la présence ou non des dates. */
    private String construireLibelleValidite(Accreditation acc) {
        if (acc.getDateDebutValidite() == null && acc.getDateFinValidite() == null) {
            return "Durée de l'événement";
        }
        if (acc.getDateDebutValidite() != null && acc.getDateFinValidite() != null) {
            return acc.getDateDebutValidite().format(FMT_DATE)
                    + " → " + acc.getDateFinValidite().format(FMT_DATE);
        }
        if (acc.getDateDebutValidite() != null) {
            return "À partir du " + acc.getDateDebutValidite().format(FMT_DATE);
        }
        return "Jusqu'au " + acc.getDateFinValidite().format(FMT_DATE);
    }

    /** Remplace les caractères non-alphanumériques pour un nom de fichier ZIP sûr. */
    private String sanitiser(String valeur) {
        return valeur == null ? "inconnu" : valeur.replaceAll("[^a-zA-Z0-9]", "_");
    }
}
