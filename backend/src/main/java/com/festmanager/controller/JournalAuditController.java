package com.festmanager.controller;

import com.festmanager.dto.JournalAuditResponse;
import com.festmanager.service.JournalAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class JournalAuditController {

    private final JournalAuditService journalAuditService;

    // Toutes les entrées (triées par timestamp décroissant)
    @GetMapping
    public ResponseEntity<Page<JournalAuditResponse>> listerTout(
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(journalAuditService.listerTout(pageable));
    }

    // Entrées pour une entité précise (ex : tous les accès à un bénévole)
    @GetMapping("/entite")
    public ResponseEntity<Page<JournalAuditResponse>> listerParEntite(
            @RequestParam String entiteCible,
            @RequestParam UUID entiteId,
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(journalAuditService.listerParEntite(entiteCible, entiteId, pageable));
    }

    // Entrées pour un utilisateur donné (qui a accédé à quoi)
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<Page<JournalAuditResponse>> listerParUtilisateur(
            @PathVariable UUID utilisateurId,
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(journalAuditService.listerParUtilisateur(utilisateurId, pageable));
    }
}
