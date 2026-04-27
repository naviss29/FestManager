package com.festmanager.service;

import com.festmanager.dto.AffectationRequest;
import com.festmanager.dto.AffectationResponse;
import com.festmanager.entity.Affectation;
import com.festmanager.entity.Benevole;
import com.festmanager.entity.Creneau;
import com.festmanager.entity.Evenement;
import com.festmanager.entity.Mission;
import com.festmanager.entity.enums.StatutAffectation;
import com.festmanager.entity.enums.StatutCompteBenevole;
import com.festmanager.mapper.AffectationMapper;
import com.festmanager.repository.AffectationRepository;
import com.festmanager.repository.BenevoleRepository;
import com.festmanager.repository.CreneauRepository;
import com.festmanager.websocket.DashboardEvent;
import com.festmanager.websocket.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AffectationService")
class AffectationServiceTest {

    @Mock AffectationRepository affectationRepository;
    @Mock BenevoleRepository benevoleRepository;
    @Mock CreneauRepository creneauRepository;
    @Mock AffectationMapper affectationMapper;
    @Mock DashboardService dashboardService;
    @Mock EmailService emailService;

    @InjectMocks AffectationService service;

    private UUID benevoleId;
    private UUID creneauId;
    private UUID affectationId;
    private Benevole benevole;
    private Creneau creneau;
    private Mission mission;
    private Evenement evenement;

    @BeforeEach
    void setUp() {
        benevoleId    = UUID.randomUUID();
        creneauId     = UUID.randomUUID();
        affectationId = UUID.randomUUID();

        evenement = new Evenement();
        evenement.setId(UUID.randomUUID());

        mission = Mission.builder()
                .id(UUID.randomUUID())
                .evenement(evenement)
                .multiAffectationAutorisee(false)
                .build();

        creneau = Creneau.builder()
                .id(creneauId)
                .mission(mission)
                .debut(LocalDateTime.of(2026, 7, 1, 10, 0))
                .fin(LocalDateTime.of(2026, 7, 1, 14, 0))
                .nbBenevolesRequis(3)
                .build();

        benevole = new Benevole();
        benevole.setId(benevoleId);
        benevole.setStatutCompte(StatutCompteBenevole.VALIDE);
    }

    // -------------------------------------------------------------------------
    // listerParCreneau
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("listerParCreneau — retourne la liste des affectations mappées")
    void listerParCreneau_retourneListe() {
        Affectation a = new Affectation();
        when(affectationRepository.findByCreneauId(creneauId)).thenReturn(List.of(a));
        when(affectationMapper.toResponse(a)).thenReturn(new AffectationResponse());

        List<AffectationResponse> result = service.listerParCreneau(creneauId);

        assertThat(result).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // listerParBenevole
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("listerParBenevole — retourne la liste des affectations du bénévole")
    void listerParBenevole_retourneListe() {
        Affectation a = new Affectation();
        when(affectationRepository.findByBenevoleId(benevoleId)).thenReturn(List.of(a));
        when(affectationMapper.toResponse(a)).thenReturn(new AffectationResponse());

        List<AffectationResponse> result = service.listerParBenevole(benevoleId);

        assertThat(result).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // obtenir
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("obtenir — lève NOT_FOUND si l'affectation est introuvable")
    void obtenir_leveNotFoundSiMissing() {
        when(affectationRepository.findByIdWithAssociations(affectationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenir(affectationId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("introuvable");
    }

    // -------------------------------------------------------------------------
    // affecter
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("affecter — crée l'affectation en statut EN_ATTENTE et notifie le dashboard")
    void affecter_creeLAffectationEtNotifie() {
        AffectationRequest request = new AffectationRequest();
        request.setBenevoleId(benevoleId);
        request.setCreneauId(creneauId);

        when(benevoleRepository.findById(benevoleId)).thenReturn(Optional.of(benevole));
        when(creneauRepository.findByIdWithMissionAndEvenement(creneauId)).thenReturn(Optional.of(creneau));
        when(affectationRepository.existsByBenevoleIdAndCreneauId(benevoleId, creneauId)).thenReturn(false);
        when(affectationRepository.countByCreneauIdAndStatut(creneauId, StatutAffectation.CONFIRME)).thenReturn(0);
        when(creneauRepository.findChevauchements(any(), any(), any(), any())).thenReturn(List.of());

        Affectation sauvegardee = new Affectation();
        when(affectationRepository.save(any())).thenReturn(sauvegardee);
        when(affectationMapper.toResponse(sauvegardee)).thenReturn(new AffectationResponse());

        AffectationResponse result = service.affecter(request);

        assertThat(result).isNotNull();
        verify(affectationRepository).save(argThat(a -> a.getStatut() == StatutAffectation.EN_ATTENTE));
        verify(dashboardService).notifierAffectation(sauvegardee, DashboardEvent.TypeEvenement.AFFECTATION_CREEE);
    }

    @Test
    @DisplayName("affecter — lève BAD_REQUEST si le bénévole est anonymisé")
    void affecter_leveBadRequestSiAnonyme() {
        benevole.setStatutCompte(StatutCompteBenevole.ANONYMISE);

        AffectationRequest request = new AffectationRequest();
        request.setBenevoleId(benevoleId);
        request.setCreneauId(creneauId);

        when(benevoleRepository.findById(benevoleId)).thenReturn(Optional.of(benevole));
        when(creneauRepository.findByIdWithMissionAndEvenement(creneauId)).thenReturn(Optional.of(creneau));

        assertThatThrownBy(() -> service.affecter(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("anonymisé");
    }

    @Test
    @DisplayName("affecter — lève CONFLICT si le bénévole est déjà affecté à ce créneau")
    void affecter_leveConflitSiDoublon() {
        AffectationRequest request = new AffectationRequest();
        request.setBenevoleId(benevoleId);
        request.setCreneauId(creneauId);

        when(benevoleRepository.findById(benevoleId)).thenReturn(Optional.of(benevole));
        when(creneauRepository.findByIdWithMissionAndEvenement(creneauId)).thenReturn(Optional.of(creneau));
        when(affectationRepository.existsByBenevoleIdAndCreneauId(benevoleId, creneauId)).thenReturn(true);

        assertThatThrownBy(() -> service.affecter(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("déjà affecté");
    }

    @Test
    @DisplayName("affecter — lève CONFLICT si le créneau est complet")
    void affecter_leveConflitSiCreneauComplet() {
        // nbBenevolesRequis = 3 et 3 déjà confirmés
        AffectationRequest request = new AffectationRequest();
        request.setBenevoleId(benevoleId);
        request.setCreneauId(creneauId);

        when(benevoleRepository.findById(benevoleId)).thenReturn(Optional.of(benevole));
        when(creneauRepository.findByIdWithMissionAndEvenement(creneauId)).thenReturn(Optional.of(creneau));
        when(affectationRepository.existsByBenevoleIdAndCreneauId(any(), any())).thenReturn(false);
        when(affectationRepository.countByCreneauIdAndStatut(creneauId, StatutAffectation.CONFIRME)).thenReturn(3);

        assertThatThrownBy(() -> service.affecter(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("complet");
    }

    @Test
    @DisplayName("affecter — lève CONFLICT si conflit horaire détecté")
    void affecter_leveConflitHoraire() {
        AffectationRequest request = new AffectationRequest();
        request.setBenevoleId(benevoleId);
        request.setCreneauId(creneauId);

        Creneau creneauEnConflit = new Creneau();
        Mission missionEnConflit = Mission.builder().nom("Mission A").build();
        creneauEnConflit.setMission(missionEnConflit);
        creneauEnConflit.setDebut(LocalDateTime.of(2026, 7, 1, 12, 0));
        creneauEnConflit.setFin(LocalDateTime.of(2026, 7, 1, 16, 0));

        when(benevoleRepository.findById(benevoleId)).thenReturn(Optional.of(benevole));
        when(creneauRepository.findByIdWithMissionAndEvenement(creneauId)).thenReturn(Optional.of(creneau));
        when(affectationRepository.existsByBenevoleIdAndCreneauId(any(), any())).thenReturn(false);
        when(affectationRepository.countByCreneauIdAndStatut(creneauId, StatutAffectation.CONFIRME)).thenReturn(0);
        when(creneauRepository.findChevauchements(any(), any(), any(), any()))
                .thenReturn(List.of(creneauEnConflit));

        assertThatThrownBy(() -> service.affecter(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Conflit horaire");
    }

    @Test
    @DisplayName("affecter — ignore les conflits horaires si multi-affectation autorisée")
    void affecter_ignoreConflitsHoraireSiMultiAutorise() {
        mission.setMultiAffectationAutorisee(true);

        AffectationRequest request = new AffectationRequest();
        request.setBenevoleId(benevoleId);
        request.setCreneauId(creneauId);

        when(benevoleRepository.findById(benevoleId)).thenReturn(Optional.of(benevole));
        when(creneauRepository.findByIdWithMissionAndEvenement(creneauId)).thenReturn(Optional.of(creneau));
        when(affectationRepository.existsByBenevoleIdAndCreneauId(any(), any())).thenReturn(false);
        when(affectationRepository.countByCreneauIdAndStatut(creneauId, StatutAffectation.CONFIRME)).thenReturn(0);
        Affectation sauvegardee = new Affectation();
        when(affectationRepository.save(any())).thenReturn(sauvegardee);
        when(affectationMapper.toResponse(sauvegardee)).thenReturn(new AffectationResponse());

        service.affecter(request);

        // findChevauchements ne doit jamais être appelé
        verify(creneauRepository, never()).findChevauchements(any(), any(), any(), any());
    }

    // -------------------------------------------------------------------------
    // changerStatut
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("changerStatut — confirme l'affectation et envoie l'email de confirmation")
    void changerStatut_confirmeEtEnvoieEmail() {
        Affectation affectation = new Affectation();
        affectation.setStatut(StatutAffectation.EN_ATTENTE);
        affectation.setCreneau(creneau);

        when(affectationRepository.findByIdWithAssociations(affectationId)).thenReturn(Optional.of(affectation));
        when(affectationRepository.countByCreneauIdAndStatut(creneauId, StatutAffectation.CONFIRME)).thenReturn(1);
        Affectation sauvegardee = new Affectation();
        when(affectationRepository.save(any())).thenReturn(sauvegardee);
        when(affectationMapper.toResponse(sauvegardee)).thenReturn(new AffectationResponse());

        service.changerStatut(affectationId, StatutAffectation.CONFIRME);

        verify(emailService).envoyerConfirmationAffectation(sauvegardee);
        verify(dashboardService).notifierAffectation(sauvegardee, DashboardEvent.TypeEvenement.AFFECTATION_MODIFIEE);
    }

    @Test
    @DisplayName("changerStatut — lève CONFLICT si le créneau est complet lors de la confirmation")
    void changerStatut_leveConflitSiCreneauCompletPourConfirmer() {
        Affectation affectation = new Affectation();
        affectation.setStatut(StatutAffectation.EN_ATTENTE);
        affectation.setCreneau(creneau);

        when(affectationRepository.findByIdWithAssociations(affectationId)).thenReturn(Optional.of(affectation));
        when(affectationRepository.countByCreneauIdAndStatut(creneauId, StatutAffectation.CONFIRME)).thenReturn(3);

        assertThatThrownBy(() -> service.changerStatut(affectationId, StatutAffectation.CONFIRME))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("complet");
    }

    @Test
    @DisplayName("changerStatut — n'envoie pas d'email si le statut n'est pas CONFIRME")
    void changerStatut_nEnvoiePasEmailSiNonConfirme() {
        Affectation affectation = new Affectation();
        affectation.setStatut(StatutAffectation.EN_ATTENTE);
        affectation.setCreneau(creneau);

        when(affectationRepository.findByIdWithAssociations(affectationId)).thenReturn(Optional.of(affectation));
        Affectation sauvegardee = new Affectation();
        when(affectationRepository.save(any())).thenReturn(sauvegardee);
        when(affectationMapper.toResponse(sauvegardee)).thenReturn(new AffectationResponse());

        service.changerStatut(affectationId, StatutAffectation.REFUSE);

        verify(emailService, never()).envoyerConfirmationAffectation(any());
    }

    // -------------------------------------------------------------------------
    // supprimer
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("supprimer — supprime l'affectation et notifie le dashboard")
    void supprimer_supprime() {
        Affectation affectation = new Affectation();
        when(affectationRepository.findByIdWithAssociations(affectationId)).thenReturn(Optional.of(affectation));

        service.supprimer(affectationId);

        verify(affectationRepository).delete(affectation);
        verify(dashboardService).notifierAffectation(affectation, DashboardEvent.TypeEvenement.AFFECTATION_SUPPRIMEE);
    }

    @Test
    @DisplayName("supprimer — lève NOT_FOUND si l'affectation est introuvable")
    void supprimer_leveNotFoundSiMissing() {
        when(affectationRepository.findByIdWithAssociations(affectationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.supprimer(affectationId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("introuvable");
    }
}
