package com.festmanager.service;

import com.festmanager.dto.MissionRequest;
import com.festmanager.dto.MissionResponse;
import com.festmanager.entity.Evenement;
import com.festmanager.entity.Mission;
import com.festmanager.entity.Organisation;
import com.festmanager.mapper.MissionMapper;
import com.festmanager.repository.EvenementRepository;
import com.festmanager.repository.MissionRepository;
import com.festmanager.repository.OrganisationRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MissionService")
class MissionServiceTest {

    @Mock MissionRepository missionRepository;
    @Mock EvenementRepository evenementRepository;
    @Mock OrganisationRepository organisationRepository;
    @Mock MissionMapper missionMapper;

    @InjectMocks MissionService service;

    private UUID evenementId;
    private UUID missionId;
    private UUID organisationId;
    private Mission mission;
    private MissionRequest request;

    @BeforeEach
    void setUp() {
        evenementId    = UUID.randomUUID();
        missionId      = UUID.randomUUID();
        organisationId = UUID.randomUUID();

        mission = new Mission();
        mission.setId(missionId);

        request = new MissionRequest();
        request.setNom("Accueil public");
        request.setCategorie("Accueil");
        request.setNbBenevolesRequis(4);
        request.setGereeParOrganisation(false);
    }

    // -------------------------------------------------------------------------
    // listerMissions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("listerMissions — retourne toutes les missions si pas de catégorie")
    void listerMissions_sansCategorie() {
        when(evenementRepository.existsById(evenementId)).thenReturn(true);
        Page<Mission> page = new PageImpl<>(List.of(mission));
        when(missionRepository.findByEvenementId(eq(evenementId), any(Pageable.class))).thenReturn(page);
        when(missionMapper.toResponse(mission)).thenReturn(new MissionResponse());

        Page<MissionResponse> result = service.listerMissions(evenementId, null, Pageable.unpaged());

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("listerMissions — filtre par catégorie si fournie")
    void listerMissions_avecFiltreCategorie() {
        when(evenementRepository.existsById(evenementId)).thenReturn(true);
        Page<Mission> page = new PageImpl<>(List.of(mission));
        when(missionRepository.findByEvenementIdAndCategorie(eq(evenementId), eq("Accueil"), any(Pageable.class)))
                .thenReturn(page);
        when(missionMapper.toResponse(mission)).thenReturn(new MissionResponse());

        service.listerMissions(evenementId, "Accueil", Pageable.unpaged());

        verify(missionRepository).findByEvenementIdAndCategorie(eq(evenementId), eq("Accueil"), any());
        verify(missionRepository, never()).findByEvenementId(any(UUID.class), any(Pageable.class));
    }

    @Test
    @DisplayName("listerMissions — lève NOT_FOUND si l'événement est introuvable")
    void listerMissions_leveNotFoundSiEvenementMissing() {
        when(evenementRepository.existsById(evenementId)).thenReturn(false);

        assertThatThrownBy(() -> service.listerMissions(evenementId, null, Pageable.unpaged()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Événement introuvable");
    }

    // -------------------------------------------------------------------------
    // creerMission
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("creerMission — crée la mission sans organisation")
    void creerMission_sansOrganisation() {
        Evenement evenement = new Evenement();
        when(evenementRepository.findById(evenementId)).thenReturn(Optional.of(evenement));
        when(missionMapper.toEntity(request, evenement, null)).thenReturn(mission);
        when(missionRepository.save(mission)).thenReturn(mission);
        when(missionMapper.toResponse(mission)).thenReturn(new MissionResponse());

        MissionResponse result = service.creerMission(evenementId, request);

        assertThat(result).isNotNull();
        verify(missionMapper).toEntity(request, evenement, null);
    }

    @Test
    @DisplayName("creerMission — crée la mission avec une organisation")
    void creerMission_avecOrganisation() {
        request.setGereeParOrganisation(true);
        request.setOrganisationId(organisationId);

        Evenement evenement = new Evenement();
        Organisation organisation = new Organisation();

        when(evenementRepository.findById(evenementId)).thenReturn(Optional.of(evenement));
        when(organisationRepository.findById(organisationId)).thenReturn(Optional.of(organisation));
        when(missionMapper.toEntity(request, evenement, organisation)).thenReturn(mission);
        when(missionRepository.save(mission)).thenReturn(mission);
        when(missionMapper.toResponse(mission)).thenReturn(new MissionResponse());

        service.creerMission(evenementId, request);

        verify(missionMapper).toEntity(request, evenement, organisation);
    }

    @Test
    @DisplayName("creerMission — lève BAD_REQUEST si gérée par organisation mais sans organisationId")
    void creerMission_leveBadRequestSiOrganisationIdManquant() {
        request.setGereeParOrganisation(true);
        request.setOrganisationId(null);

        when(evenementRepository.findById(evenementId)).thenReturn(Optional.of(new Evenement()));

        assertThatThrownBy(() -> service.creerMission(evenementId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("organisationId");
    }

    // -------------------------------------------------------------------------
    // modifierMission
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("modifierMission — met à jour les champs de la mission")
    void modifierMission_metAJour() {
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(missionRepository.save(mission)).thenReturn(mission);
        when(missionMapper.toResponse(mission)).thenReturn(new MissionResponse());

        service.modifierMission(missionId, request);

        verify(missionRepository).save(mission);
        assertThat(mission.getNom()).isEqualTo("Accueil public");
    }

    // -------------------------------------------------------------------------
    // supprimerMission
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("supprimerMission — supprime la mission existante")
    void supprimerMission_supprime() {
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        service.supprimerMission(missionId);

        verify(missionRepository).delete(mission);
    }

    @Test
    @DisplayName("supprimerMission — lève NOT_FOUND si la mission est introuvable")
    void supprimerMission_leveNotFoundSiMissing() {
        when(missionRepository.findById(missionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.supprimerMission(missionId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Mission introuvable");
    }
}
