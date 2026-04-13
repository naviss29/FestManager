package com.festmanager.service;

import com.festmanager.dto.JournalAuditResponse;
import com.festmanager.mapper.JournalAuditMapper;
import com.festmanager.repository.JournalAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JournalAuditService {

    private final JournalAuditRepository journalAuditRepository;
    private final JournalAuditMapper journalAuditMapper;

    @Transactional(readOnly = true)
    public Page<JournalAuditResponse> listerTout(Pageable pageable) {
        return journalAuditRepository.findAll(pageable)
                .map(journalAuditMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<JournalAuditResponse> listerParEntite(String entiteCible, UUID entiteId, Pageable pageable) {
        return journalAuditRepository.findByEntiteCibleAndEntiteId(entiteCible, entiteId, pageable)
                .map(journalAuditMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<JournalAuditResponse> listerParUtilisateur(UUID utilisateurId, Pageable pageable) {
        return journalAuditRepository.findByUtilisateurId(utilisateurId, pageable)
                .map(journalAuditMapper::toResponse);
    }
}
