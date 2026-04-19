package com.festmanager.service;

import com.festmanager.dto.CreneauRequest;
import com.festmanager.dto.CreneauResponse;
import com.festmanager.entity.Creneau;
import com.festmanager.entity.Mission;
import com.festmanager.entity.enums.StatutAffectation;
import com.festmanager.mapper.CreneauMapper;
import com.festmanager.repository.AffectationRepository;
import com.festmanager.repository.CreneauRepository;
import com.festmanager.repository.MissionRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreneauService")
class CreneauServiceTest {

    @Mock CreneauRepository creneauRepository;
    @Mock MissionRepository missionRepository;
    @Mock AffectationRepository affectationRepository;
    @Mock CreneauMapper creneauMapper;

    @InjectMocks CreneauService service;

    private UUID missionId;
    private UUID creneauId;
    private Creneau creneau;
    private CreneauRequest request;

    @BeforeEach
    void setUp() {
        missionId = UUID.randomUUID();
        creneauId = UUID.randomUUID();

        creneau = Creneau.builder()
                .id(creneauId)
                .debut(LocalDateTime.of(2026, 7, 1, 10, 0))
                .fin(LocalDateTime.of(2026, 7, 1, 14, 0))
                .nbBenevolesRequis(5)
                .build();

        request = new CreneauRequest();
        request.setDebut(LocalDateTime.of(2026, 7, 1, 10, 0));
        request.setFin(LocalDateTime.of(2026, 7, 1, 14, 0));
        request.setNbBenevolesRequis(5);
    }

    // -------------------------------------------------------------------------
    // listerCreneaux
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("listerCreneaux — charge les counts en une requête groupée (anti-N+1)")
    void listerCreneaux_utiliseCountBatch() {
        when(missionRepository.existsById(missionId)).thenReturn(true);
        when(creneauRepository.findByMissionId(missionId)).thenReturn(List.of(creneau));
        // La requête groupée retourne une List<Object[]> : chaque élément est [creneauId, count]
        // On utilise doReturn (au lieu de when/thenReturn) pour éviter les ambiguïtés de type Java
        java.util.List<Object[]> countsBatch = new java.util.ArrayList<>();
        countsBatch.add(new Object[]{creneauId, 2L});
        doReturn(countsBatch).when(affectationRepository)
                .countGroupedByCreneauIds(any(), eq(StatutAffectation.CONFIRME));
        when(creneauMapper.toResponse(eq(creneau), eq(2))).thenReturn(new CreneauResponse());

        List<CreneauResponse> result = service.listerCreneaux(missionId);

        assertThat(result).hasSize(1);
        // Vérifie que le count batch est utilisé (et non la surcharge sans count)
        verify(creneauMapper).toResponse(eq(creneau), eq(2));
        verify(creneauMapper, never()).toResponse(any(Creneau.class));
    }

    @Test
    @DisplayName("listerCreneaux — utilise 0 si le créneau n'a aucune affectation confirmée")
    void listerCreneaux_defaultAZeroSiPasDeCount() {
        when(missionRepository.existsById(missionId)).thenReturn(true);
        when(creneauRepository.findByMissionId(missionId)).thenReturn(List.of(creneau));
        // La requête groupée retourne une liste vide (aucune affectation CONFIRME)
        doReturn(new java.util.ArrayList<Object[]>()).when(affectationRepository)
                .countGroupedByCreneauIds(any(), any());
        when(creneauMapper.toResponse(eq(creneau), eq(0))).thenReturn(new CreneauResponse());

        service.listerCreneaux(missionId);

        verify(creneauMapper).toResponse(eq(creneau), eq(0));
    }

    @Test
    @DisplayName("listerCreneaux — lève NOT_FOUND si la mission est introuvable")
    void listerCreneaux_leveNotFoundSiMissionMissing() {
        when(missionRepository.existsById(missionId)).thenReturn(false);

        assertThatThrownBy(() -> service.listerCreneaux(missionId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Mission introuvable");
    }

    // -------------------------------------------------------------------------
    // obtenirCreneau
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("obtenirCreneau — lève NOT_FOUND si le créneau est introuvable")
    void obtenirCreneau_leveNotFoundSiMissing() {
        when(creneauRepository.findById(creneauId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenirCreneau(creneauId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Créneau introuvable");
    }

    // -------------------------------------------------------------------------
    // creerCreneau
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("creerCreneau — crée le créneau avec les bonnes données")
    void creerCreneau_creeLeCreneauAvecSucces() {
        Mission mission = new Mission();
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(creneauMapper.toEntity(request, mission)).thenReturn(creneau);
        when(creneauRepository.save(creneau)).thenReturn(creneau);
        when(creneauMapper.toResponse(creneau)).thenReturn(new CreneauResponse());

        CreneauResponse result = service.creerCreneau(missionId, request);

        assertThat(result).isNotNull();
        verify(creneauRepository).save(creneau);
    }

    @Test
    @DisplayName("creerCreneau — lève BAD_REQUEST si la fin est avant le début")
    void creerCreneau_leveBadRequestSiDatesInvalides() {
        request.setFin(LocalDateTime.of(2026, 7, 1, 8, 0)); // fin avant début
        // Pas de stub findById : validerDates() est appelée en premier et lève l'exception

        assertThatThrownBy(() -> service.creerCreneau(missionId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("fin");
    }

    // -------------------------------------------------------------------------
    // modifierCreneau
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("modifierCreneau — met à jour les champs du créneau")
    void modifierCreneau_metAJourLesChampsAvecSucces() {
        when(creneauRepository.findById(creneauId)).thenReturn(Optional.of(creneau));
        when(creneauRepository.save(creneau)).thenReturn(creneau);
        when(creneauMapper.toResponse(creneau)).thenReturn(new CreneauResponse());

        service.modifierCreneau(creneauId, request);

        verify(creneauRepository).save(creneau);
    }

    // -------------------------------------------------------------------------
    // supprimerCreneau
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("supprimerCreneau — supprime le créneau existant")
    void supprimerCreneau_supprime() {
        when(creneauRepository.findById(creneauId)).thenReturn(Optional.of(creneau));

        service.supprimerCreneau(creneauId);

        verify(creneauRepository).delete(creneau);
    }
}
