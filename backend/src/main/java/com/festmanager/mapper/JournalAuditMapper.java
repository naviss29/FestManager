package com.festmanager.mapper;

import com.festmanager.dto.JournalAuditResponse;
import com.festmanager.entity.JournalAudit;
import org.springframework.stereotype.Component;

@Component
public class JournalAuditMapper {

    public JournalAuditResponse toResponse(JournalAudit journal) {
        JournalAuditResponse response = new JournalAuditResponse();
        response.setId(journal.getId());
        response.setUtilisateurId(journal.getUtilisateur().getId());
        response.setUtilisateurEmail(journal.getUtilisateur().getEmail());
        response.setAction(journal.getAction());
        response.setEntiteCible(journal.getEntiteCible());
        response.setEntiteId(journal.getEntiteId());
        response.setIpAddress(journal.getIpAddress());
        response.setTimestamp(journal.getTimestamp());
        response.setDetail(journal.getDetail());
        return response;
    }
}
