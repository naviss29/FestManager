package com.festmanager.service;

import com.festmanager.entity.Affectation;
import com.festmanager.entity.Benevole;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service d'envoi des emails transactionnels.
 * Les envois sont asynchrones pour ne jamais bloquer une opération métier.
 *
 * En développement (profil dev), le serveur SMTP local (MailHog ou similaire)
 * intercepte les emails sans les acheminer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.expediteur}")
    private String expediteur;

    @Value("${app.email.nom-expediteur}")
    private String nomExpediteur;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    // -------------------------------------------------------------------------
    // Affectations
    // -------------------------------------------------------------------------

    /**
     * Notification au bénévole quand son affectation passe à CONFIRME.
     */
    @Async
    public void envoyerConfirmationAffectation(Affectation affectation) {
        Benevole b = affectation.getBenevole();
        String mission  = affectation.getCreneau().getMission().getNom();
        String evenement = affectation.getCreneau().getMission().getEvenement().getNom();
        String debut     = affectation.getCreneau().getDebut().format(FORMAT);
        String fin       = affectation.getCreneau().getFin().format(FORMAT);

        String sujet = "[FestManager] Votre affectation est confirmée — " + evenement;
        String corps = construireHtml(
            "Affectation confirmée \uD83C\uDF89",
            "Bonjour " + b.getPrenom() + ",",
            "<p>Votre affectation sur l'événement <strong>" + evenement + "</strong> est confirmée.</p>" +
            "<table style='border-collapse:collapse;margin:16px 0'>" +
            "<tr><td style='padding:4px 12px 4px 0;color:#555'>Mission</td><td><strong>" + mission + "</strong></td></tr>" +
            "<tr><td style='padding:4px 12px 4px 0;color:#555'>Début</td><td>" + debut + "</td></tr>" +
            "<tr><td style='padding:4px 12px 4px 0;color:#555'>Fin</td><td>" + fin + "</td></tr>" +
            "</table>" +
            "<p>Merci pour votre engagement !</p>"
        );

        envoyer(b.getEmail(), sujet, corps);
    }

    /**
     * Invitation envoyée à un bénévole pour rejoindre un événement.
     */
    @Async
    public void envoyerInvitation(Benevole benevole, String evenementNom, String lienInscription) {
        String sujet = "[FestManager] Invitation bénévole — " + evenementNom;
        String corps = construireHtml(
            "Invitation bénévole",
            "Bonjour,",
            "<p>Vous êtes invité(e) à participer en tant que bénévole à <strong>" + evenementNom + "</strong>.</p>" +
            "<p>Cliquez sur le bouton ci-dessous pour créer votre compte et confirmer votre participation :</p>" +
            "<p style='text-align:center;margin:24px 0'>" +
            "<a href='" + lienInscription + "' style='background:#1a237e;color:white;padding:12px 24px;" +
            "border-radius:4px;text-decoration:none;font-weight:bold'>Rejoindre l'événement</a></p>"
        );

        envoyer(benevole.getEmail(), sujet, corps);
    }

    /**
     * Rappel envoyé 48h avant le début d'un créneau.
     */
    @Async
    public void envoyerRappelCreneau(Affectation affectation) {
        Benevole b = affectation.getBenevole();
        String mission   = affectation.getCreneau().getMission().getNom();
        String evenement = affectation.getCreneau().getMission().getEvenement().getNom();
        String debut     = affectation.getCreneau().getDebut().format(FORMAT);
        String lieu      = affectation.getCreneau().getMission().getLieu() != null
                         ? affectation.getCreneau().getMission().getLieu() : "voir planning";

        String sujet = "[FestManager] Rappel — votre mission débute bientôt (" + evenement + ")";
        String corps = construireHtml(
            "Rappel de mission \u23F0",
            "Bonjour " + b.getPrenom() + ",",
            "<p>Votre mission <strong>" + mission + "</strong> sur <strong>" + evenement +
            "</strong> débute le <strong>" + debut + "</strong>.</p>" +
            "<p><strong>Lieu :</strong> " + lieu + "</p>" +
            "<p>À très bientôt !</p>"
        );

        envoyer(b.getEmail(), sujet, corps);
    }

    // -------------------------------------------------------------------------
    // Mot de passe oublié
    // -------------------------------------------------------------------------

    @Async
    public void envoyerResetMotDePasse(String destinataire, String lienReset) {
        String sujet = "[FestManager] Réinitialisation de votre mot de passe";
        String corps = construireHtml(
            "Réinitialisation du mot de passe",
            "Bonjour,",
            "<p>Vous avez demandé la réinitialisation de votre mot de passe FestManager.</p>" +
            "<p>Cliquez sur le bouton ci-dessous pour choisir un nouveau mot de passe :</p>" +
            "<p style='text-align:center;margin:24px 0'>" +
            "<a href='" + lienReset + "' style='background:#1a237e;color:white;padding:12px 24px;" +
            "border-radius:4px;text-decoration:none;font-weight:bold'>Réinitialiser mon mot de passe</a></p>" +
            "<p style='color:#888;font-size:0.85rem'>Ce lien est valable 1 heure. " +
            "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>"
        );
        envoyer(destinataire, sujet, corps);
    }

    // -------------------------------------------------------------------------
    // Méthodes privées
    // -------------------------------------------------------------------------

    private void envoyer(String destinataire, String sujet, String corpsHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(expediteur, nomExpediteur);
            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(corpsHtml, true);
            mailSender.send(message);
            log.debug("Email envoyé à {} : {}", destinataire, sujet);
        } catch (Exception e) {
            // Ne jamais bloquer l'opération métier si l'email échoue (SMTP down, config manquante…)
            log.warn("Échec envoi email à {} : {}", destinataire, e.getMessage());
        }
    }

    private String construireHtml(String titre, String sousTitre, String contenu) {
        return """
            <!DOCTYPE html>
            <html lang="fr"><head><meta charset="UTF-8"></head>
            <body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:0">
              <div style="background:#1a237e;padding:24px;text-align:center">
                <h1 style="color:white;margin:0;font-size:1.4rem">FestManager</h1>
              </div>
              <div style="padding:32px;background:#fafafa">
                <h2 style="color:#1a237e;margin-top:0">%s</h2>
                <p style="color:#333">%s</p>
                %s
              </div>
              <div style="padding:16px;text-align:center;font-size:0.8rem;color:#aaa">
                Vous recevez cet email car vous êtes inscrit sur FestManager.
              </div>
            </body></html>
            """.formatted(titre, sousTitre, contenu);
    }
}
