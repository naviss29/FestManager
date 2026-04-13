package com.festmanager.service;

import com.festmanager.entity.*;
import com.festmanager.entity.enums.StatutAffectation;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService")
class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @InjectMocks EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "expediteur", "noreply@festmanager.app");
        ReflectionTestUtils.setField(emailService, "nomExpediteur", "FestManager");
    }

    @Test
    @DisplayName("envoyerConfirmationAffectation — envoie un email au bénévole")
    void envoyerConfirmationAffectation_envoyeEmail() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        Affectation affectation = affectationDeTest();

        emailService.envoyerConfirmationAffectation(affectation);

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("envoyerConfirmationAffectation — ne propage pas l'exception si l'envoi échoue")
    void envoyerConfirmationAffectation_silencieuxEnCasErreur() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP down"));

        // Ne doit pas lever d'exception
        emailService.envoyerConfirmationAffectation(affectationDeTest());

        // Pas de plantage = test réussi
    }

    @Test
    @DisplayName("envoyerInvitation — envoie un email avec le lien d'inscription")
    void envoyerInvitation_envoyeEmail() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        Benevole b = benevole();

        emailService.envoyerInvitation(b, "HellFest 2026", "http://app/register?token=abc");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("envoyerRappelCreneau — envoie un email de rappel")
    void envoyerRappelCreneau_envoyeEmail() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.envoyerRappelCreneau(affectationDeTest());

        verify(mailSender).send(mimeMessage);
    }

    // --- Helpers ---

    private Affectation affectationDeTest() {
        Evenement evenement = new Evenement();
        evenement.setId(UUID.randomUUID());
        evenement.setNom("HellFest 2026");

        Mission mission = new Mission();
        mission.setId(UUID.randomUUID());
        mission.setNom("Accueil entrée");
        mission.setLieu("Porte principale");
        mission.setEvenement(evenement);

        Creneau creneau = new Creneau();
        creneau.setId(UUID.randomUUID());
        creneau.setMission(mission);
        creneau.setDebut(LocalDateTime.of(2026, 6, 20, 8, 0));
        creneau.setFin(LocalDateTime.of(2026, 6, 20, 14, 0));

        Affectation a = new Affectation();
        a.setId(UUID.randomUUID());
        a.setBenevole(benevole());
        a.setCreneau(creneau);
        a.setStatut(StatutAffectation.CONFIRME);
        return a;
    }

    private Benevole benevole() {
        Benevole b = new Benevole();
        b.setId(UUID.randomUUID());
        b.setNom("Dupont");
        b.setPrenom("Marie");
        b.setEmail("marie.dupont@test.fr");
        return b;
    }
}
