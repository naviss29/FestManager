package com.festmanager.dto;

import com.festmanager.entity.enums.ActionAudit;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class JournalAuditResponse {

    private UUID id;
    private UUID utilisateurId;
    private String utilisateurEmail;
    private ActionAudit action;
    private String entiteCible;
    private UUID entiteId;
    private String ipAddress;
    private LocalDateTime timestamp;
    private String detail;
}
