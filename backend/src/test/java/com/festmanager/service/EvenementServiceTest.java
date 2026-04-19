package com.festmanager.service;

import com.festmanager.dto.EvenementRequest;
import com.festmanager.dto.EvenementResponse;
import com.festmanager.entity.Evenement;
import com.festmanager.entity.Utilisateur;
import com.festmanager.entity.enums.RoleUtilisateur;
import com.festmanager.entity.enums.StatutEvenement;
import com.festmanager.mapper.EvenementMapper;
import com.festmanager.repository.EvenementRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EvenementService")
class EvenementServiceTest {

    @Mock EvenementRepository evenementRepository;
    @Mock UtilisateurRepository utilisateurRepository;
    @Mock EvenementMapper evenementMapper;

    @InjectMocks EvenementService service;

    private static final String EMAIL_ORGANISATEUR = "orga@test.fr";

    private UUID evenementId;
    private Utilisateur organisateur;
    private Evenement evenement;
    private EvenementRequest request;

    @BeforeEach
    void setUp() {
        // Injecte un utilisateur authentifié dans le SecurityContext pour les méthodes
        // qui appellent SecurityContextHolder.getContext().getAuthentication().getName()
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL_ORGANISATEUR, null, Collections.emptyList())
        );

        evenementId  = UUID.randomUUID();
        organisateur = Utilisateur.builder()
                .id(UUID.randomUUID())
                .email(EMAIL_ORGANISATEUR)
                .role(RoleUtilisateur.ORGANISATEUR)
                .build();

        evenement = new Evenement();
        evenement.setId(evenementId);
        evenement.setOrganisateur(organisateur);
        evenement.setStatut(StatutEvenement.BROUILLON);

        request = new EvenementRequest();
        request.setNom("FestTest 2026");
        request.setDateDebut(LocalDate.of(2026, 7, 1));
        request.setDateFin(LocalDate.of(2026, 7, 3));
        request.setLieu("Scène principale");
    }

    @AfterEach
    void tearDown() {
        // Nettoyage du SecurityContext après chaque test pour éviter les effets de bord
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // obtenirEvenement
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("obtenirEvenement — retourne l'événement mappé")
    void obtenirEvenement_retourneLevenement() {
        when(evenementRepository.findById(evenementId)).thenReturn(Optional.of(evenement));
        when(evenementMapper.toResponse(evenement)).thenReturn(new EvenementResponse());

        EvenementResponse result = service.obtenirEvenement(evenementId);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("obtenirEvenement — lève NOT_FOUND si introuvable")
    void obtenirEvenement_leveNotFoundSiMissing() {
        when(evenementRepository.findById(evenementId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenirEvenement(evenementId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("introuvable");
    }

    // -------------------------------------------------------------------------
    // creerEvenement
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("creerEvenement — crée l'événement avec l'organisateur courant")
    void creerEvenement_creeAvecOrganisateurCourant() {
        when(utilisateurRepository.findByEmail(EMAIL_ORGANISATEUR)).thenReturn(Optional.of(organisateur));
        when(evenementMapper.toEntity(request, organisateur)).thenReturn(evenement);
        when(evenementRepository.save(evenement)).thenReturn(evenement);
        when(evenementMapper.toResponse(evenement)).thenReturn(new EvenementResponse());

        EvenementResponse result = service.creerEvenement(request);

        assertThat(result).isNotNull();
        verify(evenementMapper).toEntity(request, organisateur);
    }

    @Test
    @DisplayName("creerEvenement — lève BAD_REQUEST si la date de fin est avant la date de début")
    void creerEvenement_leveBadRequestSiDatesInvalides() {
        request.setDateFin(LocalDate.of(2026, 6, 28)); // fin avant début
        // Pas de stub utilisateur : validerDates() est appelée en premier et lève l'exception

        assertThatThrownBy(() -> service.creerEvenement(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("fin");
    }

    // -------------------------------------------------------------------------
    // modifierEvenement
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("modifierEvenement — l'organisateur peut modifier son propre événement")
    void modifierEvenement_organisateurPeutModifier() {
        when(evenementRepository.findById(evenementId)).thenReturn(Optional.of(evenement));
        when(utilisateurRepository.findByEmail(EMAIL_ORGANISATEUR)).thenReturn(Optional.of(organisateur));
        when(evenementRepository.save(evenement)).thenReturn(evenement);
        when(evenementMapper.toResponse(evenement)).thenReturn(new EvenementResponse());

        service.modifierEvenement(evenementId, request);

        verify(evenementRepository).save(evenement);
    }

    @Test
    @DisplayName("modifierEvenement — l'ADMIN peut modifier n'importe quel événement")
    void modifierEvenement_adminPeutModifier() {
        Utilisateur admin = Utilisateur.builder()
                .id(UUID.randomUUID())
                .email(EMAIL_ORGANISATEUR)
                .role(RoleUtilisateur.ADMIN)
                .build();

        when(evenementRepository.findById(evenementId)).thenReturn(Optional.of(evenement));
        when(utilisateurRepository.findByEmail(EMAIL_ORGANISATEUR)).thenReturn(Optional.of(admin));
        when(evenementRepository.save(evenement)).thenReturn(evenement);
        when(evenementMapper.toResponse(evenement)).thenReturn(new EvenementResponse());

        service.modifierEvenement(evenementId, request);

        verify(evenementRepository).save(evenement);
    }

    @Test
    @DisplayName("modifierEvenement — lève FORBIDDEN si l'utilisateur n'est pas l'organisateur de l'événement")
    void modifierEvenement_autreOrganisateurEtRefuse() {
        // Utilisateur avec un ID différent de l'organisateur de l'événement
        Utilisateur autre = Utilisateur.builder()
                .id(UUID.randomUUID())
                .email(EMAIL_ORGANISATEUR)
                .role(RoleUtilisateur.ORGANISATEUR)
                .build();

        when(evenementRepository.findById(evenementId)).thenReturn(Optional.of(evenement));
        when(utilisateurRepository.findByEmail(EMAIL_ORGANISATEUR)).thenReturn(Optional.of(autre));

        assertThatThrownBy(() -> service.modifierEvenement(evenementId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("refusé");
    }

    // -------------------------------------------------------------------------
    // changerStatut
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("changerStatut — publie l'événement (BROUILLON → PUBLIE)")
    void changerStatut_publieLevenement() {
        when(evenementRepository.findById(evenementId)).thenReturn(Optional.of(evenement));
        when(utilisateurRepository.findByEmail(EMAIL_ORGANISATEUR)).thenReturn(Optional.of(organisateur));
        when(evenementRepository.save(evenement)).thenReturn(evenement);
        when(evenementMapper.toResponse(evenement)).thenReturn(new EvenementResponse());

        service.changerStatut(evenementId, StatutEvenement.PUBLIE);

        verify(evenementRepository).save(argThat(e -> e.getStatut() == StatutEvenement.PUBLIE));
    }

    // -------------------------------------------------------------------------
    // supprimerEvenement
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("supprimerEvenement — l'organisateur supprime son événement")
    void supprimerEvenement_organisateurPeutSupprimer() {
        when(evenementRepository.findById(evenementId)).thenReturn(Optional.of(evenement));
        when(utilisateurRepository.findByEmail(EMAIL_ORGANISATEUR)).thenReturn(Optional.of(organisateur));

        service.supprimerEvenement(evenementId);

        verify(evenementRepository).delete(evenement);
    }
}
