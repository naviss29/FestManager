package com.festmanager.service;

import com.festmanager.dto.JournalAuditResponse;
import com.festmanager.entity.JournalAudit;
import com.festmanager.mapper.JournalAuditMapper;
import com.festmanager.repository.JournalAuditRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JournalAuditService")
class JournalAuditServiceTest {

    @Mock JournalAuditRepository journalAuditRepository;
    @Mock JournalAuditMapper journalAuditMapper;

    @InjectMocks JournalAuditService service;

    private final JournalAudit entree = new JournalAudit();
    private final JournalAuditResponse response = new JournalAuditResponse();

    // -------------------------------------------------------------------------
    // listerTout
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("listerTout — délègue au repository et mappe les résultats")
    void listerTout_delegueAuRepository() {
        when(journalAuditRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entree)));
        when(journalAuditMapper.toResponse(entree)).thenReturn(response);

        Page<JournalAuditResponse> result = service.listerTout(Pageable.unpaged());

        assertThat(result).hasSize(1);
        verify(journalAuditRepository).findAll(any(Pageable.class));
    }

    // -------------------------------------------------------------------------
    // listerParEntite
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("listerParEntite — filtre par entité cible et ID")
    void listerParEntite_filtreParcibleEtId() {
        UUID entiteId = UUID.randomUUID();
        when(journalAuditRepository.findByEntiteCibleAndEntiteId(eq("BENEVOLE"), eq(entiteId), any()))
                .thenReturn(new PageImpl<>(List.of(entree)));
        when(journalAuditMapper.toResponse(entree)).thenReturn(response);

        Page<JournalAuditResponse> result = service.listerParEntite("BENEVOLE", entiteId, Pageable.unpaged());

        assertThat(result).hasSize(1);
        verify(journalAuditRepository).findByEntiteCibleAndEntiteId(eq("BENEVOLE"), eq(entiteId), any());
    }

    // -------------------------------------------------------------------------
    // listerParUtilisateur
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("listerParUtilisateur — retourne les entrées d'audit d'un utilisateur donné")
    void listerParUtilisateur_filtreParUtilisateur() {
        UUID utilisateurId = UUID.randomUUID();
        when(journalAuditRepository.findByUtilisateurId(eq(utilisateurId), any()))
                .thenReturn(new PageImpl<>(List.of(entree)));
        when(journalAuditMapper.toResponse(entree)).thenReturn(response);

        Page<JournalAuditResponse> result = service.listerParUtilisateur(utilisateurId, Pageable.unpaged());

        assertThat(result).hasSize(1);
        verify(journalAuditRepository).findByUtilisateurId(eq(utilisateurId), any());
    }
}
