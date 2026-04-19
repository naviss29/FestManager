package com.festmanager.audit;

import com.festmanager.entity.JournalAudit;
import com.festmanager.entity.Utilisateur;
import com.festmanager.entity.enums.ActionAudit;
import com.festmanager.repository.JournalAuditRepository;
import com.festmanager.repository.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService")
class AuditServiceTest {

    @Mock JournalAuditRepository journalAuditRepository;
    @Mock UtilisateurRepository utilisateurRepository;

    @InjectMocks AuditService service;

    private static final String EMAIL = "user@test.fr";
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, null, Collections.emptyList())
        );

        utilisateur = Utilisateur.builder()
                .id(UUID.randomUUID())
                .email(EMAIL)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // tracer
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("tracer — enregistre l'entrée d'audit avec tous les champs attendus")
    void tracer_sauvegardeLEntreeAvecLesBonsChamps() {
        UUID entiteId = UUID.randomUUID();
        when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(utilisateur));

        service.tracer(ActionAudit.LECTURE, "BENEVOLE", entiteId, "127.0.0.1", "test");

        verify(journalAuditRepository).save(argThat((JournalAudit j) ->
                j.getAction() == ActionAudit.LECTURE
                && "BENEVOLE".equals(j.getEntiteCible())
                && entiteId.equals(j.getEntiteId())
                && "127.0.0.1".equals(j.getIpAddress())
                && "test".equals(j.getDetail())
                && j.getUtilisateur() == utilisateur
        ));
    }

    @Test
    @DisplayName("tracer — ne lève pas d'exception si l'utilisateur est introuvable en base")
    void tracer_silencieuxSiUtilisateurIntrouvable() {
        when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // Ne doit pas lever d'exception
        service.tracer(ActionAudit.LECTURE, "BENEVOLE", UUID.randomUUID(), null, null);

        verify(journalAuditRepository, never()).save(any());
    }

    @Test
    @DisplayName("tracer — ne lève pas d'exception si le SecurityContext est vide")
    void tracer_silencieuxSiSecurityContextVide() {
        SecurityContextHolder.clearContext(); // Pas d'utilisateur authentifié

        // Ne doit pas lever d'exception — l'audit ne bloque jamais l'appel métier
        service.tracer(ActionAudit.LECTURE, "BENEVOLE", UUID.randomUUID(), null, null);

        verify(journalAuditRepository, never()).save(any());
    }

    @Test
    @DisplayName("tracer (surcharge sans IP/détail) — délègue à la méthode principale")
    void tracerSurcharge_delegueALaMethodePrincipale() {
        UUID entiteId = UUID.randomUUID();
        when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(utilisateur));

        service.tracer(ActionAudit.CREATION, "EVENEMENT", entiteId);

        verify(journalAuditRepository).save(argThat((JournalAudit j) ->
                j.getAction() == ActionAudit.CREATION
                && j.getIpAddress() == null
                && j.getDetail() == null
        ));
    }
}
