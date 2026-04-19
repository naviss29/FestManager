package com.festmanager.service;

import com.festmanager.audit.AuditService;
import com.festmanager.dto.BenevoleInvitationRequest;
import com.festmanager.dto.BenevoleRequest;
import com.festmanager.dto.BenevoleResponse;
import com.festmanager.entity.Benevole;
import com.festmanager.entity.enums.ActionAudit;
import com.festmanager.entity.enums.StatutCompteBenevole;
import com.festmanager.mapper.BenevoleMapper;
import com.festmanager.repository.BenevoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BenevoleService")
class BenevoleServiceTest {

    @Mock BenevoleRepository benevoleRepository;
    @Mock BenevoleMapper benevoleMapper;
    @Mock AuditService auditService;

    @InjectMocks BenevoleService service;

    private UUID id;
    private Benevole benevole;
    private BenevoleRequest request;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();

        benevole = new Benevole();
        benevole.setId(id);
        benevole.setNom("Dupont");
        benevole.setPrenom("Marie");
        benevole.setEmail("marie@test.fr");
        benevole.setStatutCompte(StatutCompteBenevole.INSCRIT);
        benevole.setConsentementRgpd(true);
        benevole.setDateConsentement(LocalDateTime.now());

        request = new BenevoleRequest();
        request.setNom("Dupont");
        request.setPrenom("Marie");
        request.setEmail("marie@test.fr");
    }

    // -------------------------------------------------------------------------
    // obtenirBenevole
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("obtenirBenevole — retourne le bénévole et trace l'accès dans l'audit")
    void obtenirBenevole_retourneEtTrace() {
        when(benevoleRepository.findById(id)).thenReturn(Optional.of(benevole));
        when(benevoleMapper.toResponse(benevole)).thenReturn(new BenevoleResponse());

        service.obtenirBenevole(id, "127.0.0.1");

        verify(auditService).tracer(eq(ActionAudit.LECTURE), eq("BENEVOLE"), eq(id), eq("127.0.0.1"), isNull());
    }

    @Test
    @DisplayName("obtenirBenevole — lève NOT_FOUND si introuvable")
    void obtenirBenevole_leveNotFoundSiMissing() {
        when(benevoleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenirBenevole(id, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("introuvable");
    }

    // -------------------------------------------------------------------------
    // inscrire (flux 1 : inscription libre)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("inscrire — crée le bénévole avec le statut INSCRIT")
    void inscrire_creeAvecStatutInscrit() {
        when(benevoleRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(benevoleMapper.toEntity(request, StatutCompteBenevole.INSCRIT)).thenReturn(benevole);
        when(benevoleRepository.save(benevole)).thenReturn(benevole);
        when(benevoleMapper.toResponse(benevole)).thenReturn(new BenevoleResponse());

        service.inscrire(request);

        verify(benevoleMapper).toEntity(request, StatutCompteBenevole.INSCRIT);
        verify(auditService).tracer(eq(ActionAudit.CREATION), eq("BENEVOLE"), any(), isNull(), eq("inscription libre"));
    }

    @Test
    @DisplayName("inscrire — lève CONFLICT si l'email est déjà utilisé")
    void inscrire_leveConflitSiEmailPris() {
        when(benevoleRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> service.inscrire(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("email");
    }

    // -------------------------------------------------------------------------
    // creerManuellement (flux 2 : création par organisateur)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("creerManuellement — crée le bénévole avec le statut VALIDE")
    void creerManuellement_creeAvecStatutValide() {
        when(benevoleRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(benevoleMapper.toEntity(request, StatutCompteBenevole.VALIDE)).thenReturn(benevole);
        when(benevoleRepository.save(benevole)).thenReturn(benevole);
        when(benevoleMapper.toResponse(benevole)).thenReturn(new BenevoleResponse());

        service.creerManuellement(request, "10.0.0.1");

        verify(benevoleMapper).toEntity(request, StatutCompteBenevole.VALIDE);
        verify(auditService).tracer(eq(ActionAudit.CREATION), eq("BENEVOLE"), any(), eq("10.0.0.1"), eq("création manuelle"));
    }

    // -------------------------------------------------------------------------
    // inviter (flux 3 : invitation email)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("inviter — crée le bénévole avec le statut INVITE et sans consentement RGPD")
    void inviter_creeAvecStatutInvite() {
        BenevoleInvitationRequest invitRequest = new BenevoleInvitationRequest();
        invitRequest.setNom("Martin");
        invitRequest.setPrenom("Paul");
        invitRequest.setEmail("paul@test.fr");

        when(benevoleRepository.existsByEmail("paul@test.fr")).thenReturn(false);
        when(benevoleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(benevoleMapper.toResponse(any())).thenReturn(new BenevoleResponse());

        service.inviter(invitRequest, "10.0.0.1");

        verify(benevoleRepository).save(argThat(b ->
                b.getStatutCompte() == StatutCompteBenevole.INVITE
                && !b.getConsentementRgpd()
        ));
    }

    // -------------------------------------------------------------------------
    // modifier
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("modifier — met à jour les champs et trace la modification")
    void modifier_metAJourEtTrace() {
        request.setEmail("nouveau@test.fr");
        when(benevoleRepository.findById(id)).thenReturn(Optional.of(benevole));
        when(benevoleRepository.existsByEmail("nouveau@test.fr")).thenReturn(false);
        when(benevoleRepository.save(any())).thenReturn(benevole);
        when(benevoleMapper.toResponse(benevole)).thenReturn(new BenevoleResponse());

        service.modifier(id, request, "10.0.0.1");

        verify(auditService).tracer(eq(ActionAudit.MODIFICATION), eq("BENEVOLE"), eq(id), eq("10.0.0.1"), any());
    }

    @Test
    @DisplayName("modifier — lève CONFLICT si le nouvel email est déjà utilisé par un autre compte")
    void modifier_leveConflitSiNouvelEmailPris() {
        request.setEmail("autre@test.fr"); // email différent de l'existant
        when(benevoleRepository.findById(id)).thenReturn(Optional.of(benevole));
        when(benevoleRepository.existsByEmail("autre@test.fr")).thenReturn(true);

        assertThatThrownBy(() -> service.modifier(id, request, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("email");
    }

    // -------------------------------------------------------------------------
    // exporterDonnees (RGPD Art. 15)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("exporterDonnees — retourne toutes les données personnelles et trace l'export")
    void exporterDonnees_retourneLesDonneesEtTrace() {
        when(benevoleRepository.findById(id)).thenReturn(Optional.of(benevole));

        Map<String, Object> export = service.exporterDonnees(id, "10.0.0.1");

        assertThat(export).containsKeys("nom", "prenom", "email", "consentementRgpd", "versionCgu");
        assertThat(export.get("nom")).isEqualTo("Dupont");
        verify(auditService).tracer(eq(ActionAudit.EXPORT), eq("BENEVOLE"), eq(id), eq("10.0.0.1"), any());
    }

    // -------------------------------------------------------------------------
    // anonymiser (RGPD Art. 17)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("anonymiser — remplace les données personnelles par des valeurs neutres")
    void anonymiser_remplaceLesDonneesPersoDuBenevole() {
        when(benevoleRepository.findById(id)).thenReturn(Optional.of(benevole));
        when(benevoleRepository.save(any())).thenReturn(benevole);

        service.anonymiser(id, "10.0.0.1");

        verify(benevoleRepository).save(argThat(b ->
                "ANONYME".equals(b.getNom())
                && "ANONYME".equals(b.getPrenom())
                && b.getEmail().contains("anonyme-")
                && b.getStatutCompte() == StatutCompteBenevole.ANONYMISE
                && b.getDateAnonymisation() != null
        ));
        verify(auditService).tracer(eq(ActionAudit.ANONYMISATION), eq("BENEVOLE"), eq(id), eq("10.0.0.1"), any());
    }

    @Test
    @DisplayName("anonymiser — lève CONFLICT si le bénévole est déjà anonymisé")
    void anonymiser_leveConflitSiDejaAnonymise() {
        benevole.setStatutCompte(StatutCompteBenevole.ANONYMISE);
        when(benevoleRepository.findById(id)).thenReturn(Optional.of(benevole));

        assertThatThrownBy(() -> service.anonymiser(id, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("déjà anonymisé");
    }
}
