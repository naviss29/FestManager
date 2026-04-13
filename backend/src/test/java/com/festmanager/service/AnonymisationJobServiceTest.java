package com.festmanager.service;

import com.festmanager.entity.Benevole;
import com.festmanager.entity.enums.StatutCompteBenevole;
import com.festmanager.repository.BenevoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnonymisationJobService")
class AnonymisationJobServiceTest {

    @Mock BenevoleRepository benevoleRepository;
    @Mock BenevoleService benevoleService;

    @InjectMocks
    AnonymisationJobService job;

    @Test
    @DisplayName("lancerPurgeRgpd — anonymise chaque bénévole éligible")
    void lancerPurgeRgpd_anonymiseEligibles() {
        Benevole b1 = benevole();
        Benevole b2 = benevole();
        when(benevoleRepository.findEligiblesAnonymisation(any(), any())).thenReturn(List.of(b1, b2));

        job.lancerPurgeRgpd();

        verify(benevoleService, times(2)).anonymiser(any(), eq("job-automatique"));
    }

    @Test
    @DisplayName("lancerPurgeRgpd — ne fait rien si aucun éligible")
    void lancerPurgeRgpd_sansEligibles_neFaitRien() {
        when(benevoleRepository.findEligiblesAnonymisation(any(), any())).thenReturn(List.of());

        job.lancerPurgeRgpd();

        verify(benevoleService, never()).anonymiser(any(), any());
    }

    @Test
    @DisplayName("lancerPurgeRgpd — continue si un bénévole lève une exception")
    void lancerPurgeRgpd_continueApresErreur() {
        Benevole b1 = benevole();
        Benevole b2 = benevole();
        when(benevoleRepository.findEligiblesAnonymisation(any(), any())).thenReturn(List.of(b1, b2));

        // Le premier bénévole lève une exception
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "déjà anonymisé"))
            .when(benevoleService).anonymiser(eq(b1.getId()), any());

        // Ne doit pas propager l'exception et doit traiter b2
        job.lancerPurgeRgpd();

        verify(benevoleService).anonymiser(b2.getId(), "job-automatique");
    }

    private Benevole benevole() {
        Benevole b = new Benevole();
        b.setId(UUID.randomUUID());
        b.setStatutCompte(StatutCompteBenevole.VALIDE);
        return b;
    }
}
