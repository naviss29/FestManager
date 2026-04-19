package com.festmanager.service;

import com.festmanager.dto.OrganisationRequest;
import com.festmanager.dto.OrganisationResponse;
import com.festmanager.entity.Organisation;
import com.festmanager.entity.Utilisateur;
import com.festmanager.entity.enums.RoleUtilisateur;
import com.festmanager.entity.enums.TypeOrganisation;
import com.festmanager.mapper.OrganisationMapper;
import com.festmanager.repository.OrganisationRepository;
import com.festmanager.repository.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrganisationService")
class OrganisationServiceTest {

    @Mock OrganisationRepository organisationRepository;
    @Mock UtilisateurRepository utilisateurRepository;
    @Mock OrganisationMapper organisationMapper;

    @InjectMocks OrganisationService service;

    private static final String EMAIL = "user@test.fr";

    private UUID organisationId;
    private Organisation organisation;
    private Utilisateur admin;
    private OrganisationRequest request;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, null, Collections.emptyList())
        );

        organisationId = UUID.randomUUID();

        organisation = new Organisation();
        organisation.setId(organisationId);
        organisation.setNom("Asso Fête");

        admin = Utilisateur.builder()
                .id(UUID.randomUUID())
                .email(EMAIL)
                .role(RoleUtilisateur.ADMIN)
                .build();

        request = new OrganisationRequest();
        request.setNom("Asso Fête");
        request.setType(TypeOrganisation.ASSOCIATION);
        request.setEmailContact("contact@asso.fr");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // listerOrganisations
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("listerOrganisations — l'ADMIN voit toutes les organisations")
    void listerOrganisations_adminVoitTout() {
        when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(admin));
        Page<Organisation> page = new PageImpl<>(List.of(organisation));
        when(organisationRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(organisationMapper.toResponse(organisation)).thenReturn(new OrganisationResponse());

        Page<OrganisationResponse> result = service.listerOrganisations(null, Pageable.unpaged());

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("listerOrganisations — le REFERENT ne voit que son organisation")
    void listerOrganisations_referentVoitSeulemantLaSienne() {
        Utilisateur referent = Utilisateur.builder()
                .id(UUID.randomUUID())
                .email(EMAIL)
                .role(RoleUtilisateur.REFERENT_ORGANISATION)
                .organisation(organisation)
                .build();

        when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(referent));
        when(organisationMapper.toResponse(organisation)).thenReturn(new OrganisationResponse());

        Page<OrganisationResponse> result = service.listerOrganisations(null, Pageable.unpaged());

        assertThat(result).hasSize(1);
        // Le repository ne doit pas être consulté pour le référent
        verify(organisationRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("listerOrganisations — le REFERENT sans organisation reçoit FORBIDDEN")
    void listerOrganisations_referentSansOrganisationRecoitForbidden() {
        Utilisateur referentSansOrg = Utilisateur.builder()
                .id(UUID.randomUUID())
                .email(EMAIL)
                .role(RoleUtilisateur.REFERENT_ORGANISATION)
                .organisation(null)
                .build();

        when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(referentSansOrg));

        assertThatThrownBy(() -> service.listerOrganisations(null, Pageable.unpaged()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("organisation");
    }

    // -------------------------------------------------------------------------
    // obtenirOrganisation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("obtenirOrganisation — le REFERENT accède à sa propre organisation")
    void obtenirOrganisation_referentAccedeALaSienne() {
        Utilisateur referent = Utilisateur.builder()
                .id(UUID.randomUUID())
                .email(EMAIL)
                .role(RoleUtilisateur.REFERENT_ORGANISATION)
                .organisation(organisation)
                .build();

        when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(referent));
        when(organisationRepository.findById(organisationId)).thenReturn(Optional.of(organisation));
        when(organisationMapper.toResponse(organisation)).thenReturn(new OrganisationResponse());

        OrganisationResponse result = service.obtenirOrganisation(organisationId);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("obtenirOrganisation — le REFERENT est refusé s'il accède à une autre organisation")
    void obtenirOrganisation_referentRefuseSurAutreOrganisation() {
        Organisation autreOrg = new Organisation();
        autreOrg.setId(UUID.randomUUID());

        Utilisateur referent = Utilisateur.builder()
                .id(UUID.randomUUID())
                .email(EMAIL)
                .role(RoleUtilisateur.REFERENT_ORGANISATION)
                .organisation(autreOrg) // associé à une organisation différente
                .build();

        when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(referent));

        assertThatThrownBy(() -> service.obtenirOrganisation(organisationId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("organisation");
    }

    // -------------------------------------------------------------------------
    // creerOrganisation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("creerOrganisation — crée l'organisation avec succès")
    void creerOrganisation_creeAvecSucces() {
        when(organisationRepository.existsByEmailContact("contact@asso.fr")).thenReturn(false);
        when(organisationMapper.toEntity(request)).thenReturn(organisation);
        when(organisationRepository.save(organisation)).thenReturn(organisation);
        when(organisationMapper.toResponse(organisation)).thenReturn(new OrganisationResponse());

        OrganisationResponse result = service.creerOrganisation(request);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("creerOrganisation — lève CONFLICT si l'email de contact est déjà utilisé")
    void creerOrganisation_leveConflitSiEmailPris() {
        when(organisationRepository.existsByEmailContact("contact@asso.fr")).thenReturn(true);

        assertThatThrownBy(() -> service.creerOrganisation(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("email");
    }

    // -------------------------------------------------------------------------
    // supprimerOrganisation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("supprimerOrganisation — supprime l'organisation existante")
    void supprimerOrganisation_supprime() {
        // supprimerOrganisation ne vérifie pas le rôle — pas de stub utilisateur nécessaire
        when(organisationRepository.findById(organisationId)).thenReturn(Optional.of(organisation));

        service.supprimerOrganisation(organisationId);

        verify(organisationRepository).delete(organisation);
    }
}
