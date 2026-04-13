package com.festmanager.service;

import com.festmanager.dto.AccreditationRequest;
import com.festmanager.dto.AccreditationResponse;
import com.festmanager.entity.Accreditation;
import com.festmanager.entity.Benevole;
import com.festmanager.entity.Evenement;
import com.festmanager.entity.enums.TypeAccreditation;
import com.festmanager.mapper.AccreditationMapper;
import com.festmanager.repository.AccreditationRepository;
import com.festmanager.repository.BenevoleRepository;
import com.festmanager.repository.EvenementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccreditationService")
class AccreditationServiceTest {

    @Mock AccreditationRepository accreditationRepository;
    @Mock BenevoleRepository benevoleRepository;
    @Mock EvenementRepository evenementRepository;
    @Mock AccreditationMapper accreditationMapper;
    @Mock QrCodeService qrCodeService;

    @InjectMocks
    AccreditationService service;

    private UUID benevoleId;
    private UUID evenementId;
    private Benevole benevole;
    private Evenement evenement;

    @BeforeEach
    void setUp() {
        benevoleId  = UUID.randomUUID();
        evenementId = UUID.randomUUID();
        benevole    = new Benevole();
        evenement   = new Evenement();
    }

    @Test
    @DisplayName("creer — crée l'accréditation et génère un code QR")
    void creer_creeAccreditationAvecCodeQr() {
        AccreditationRequest request = new AccreditationRequest();
        request.setBenevoleId(benevoleId);
        request.setEvenementId(evenementId);
        request.setType(TypeAccreditation.BENEVOLE);
        request.setZonesAcces(Set.of());

        when(accreditationRepository.existsByBenevoleIdAndEvenementId(benevoleId, evenementId)).thenReturn(false);
        when(benevoleRepository.findById(benevoleId)).thenReturn(Optional.of(benevole));
        when(evenementRepository.findById(evenementId)).thenReturn(Optional.of(evenement));

        Accreditation sauvegardee = new Accreditation();
        when(accreditationRepository.save(any())).thenReturn(sauvegardee);
        when(accreditationMapper.toResponse(sauvegardee)).thenReturn(new AccreditationResponse());

        AccreditationResponse result = service.creer(request);

        assertThat(result).isNotNull();
        verify(accreditationRepository).save(argThat(a ->
            a.getCodeQr() != null && a.getCodeQr().startsWith("FESTMANAGER:")
        ));
    }

    @Test
    @DisplayName("creer — lève CONFLICT si une accréditation existe déjà")
    void creer_leveConflitSiExisteDeja() {
        AccreditationRequest request = new AccreditationRequest();
        request.setBenevoleId(benevoleId);
        request.setEvenementId(evenementId);
        request.setType(TypeAccreditation.STAFF);

        when(accreditationRepository.existsByBenevoleIdAndEvenementId(benevoleId, evenementId)).thenReturn(true);

        assertThatThrownBy(() -> service.creer(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("déjà une accréditation");
    }

    @Test
    @DisplayName("creer — lève NOT_FOUND si le bénévole est introuvable")
    void creer_leveNotFoundSiBenevoleMissing() {
        AccreditationRequest request = new AccreditationRequest();
        request.setBenevoleId(benevoleId);
        request.setEvenementId(evenementId);
        request.setType(TypeAccreditation.BENEVOLE);

        when(accreditationRepository.existsByBenevoleIdAndEvenementId(any(), any())).thenReturn(false);
        when(benevoleRepository.findById(benevoleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.creer(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Bénévole introuvable");
    }

    @Test
    @DisplayName("supprimer — appelle delete sur le repository")
    void supprimer_appelleDelete() {
        UUID id = UUID.randomUUID();
        Accreditation accreditation = new Accreditation();
        when(accreditationRepository.findById(id)).thenReturn(Optional.of(accreditation));

        service.supprimer(id);

        verify(accreditationRepository).delete(accreditation);
    }

    @Test
    @DisplayName("obtenir — lève NOT_FOUND si l'accréditation est introuvable")
    void obtenir_leveNotFoundSiMissing() {
        UUID id = UUID.randomUUID();
        when(accreditationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenir(id))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("introuvable");
    }
}
