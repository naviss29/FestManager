package com.festmanager.controller;

import com.festmanager.dto.JournalAuditResponse;
import com.festmanager.service.JournalAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Journal d'audit RGPD", description = "Traçabilité de tous les accès aux données personnelles — réservé ADMIN")
public class JournalAuditController {

    private final JournalAuditService journalAuditService;

    @Operation(summary = "Lister toutes les entrées d'audit", description = "Triées par timestamp décroissant.")
    // Toutes les entrées (triées par timestamp décroissant)
    @GetMapping
    public ResponseEntity<Page<JournalAuditResponse>> listerTout(
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(journalAuditService.listerTout(pageable));
    }

    @Operation(summary = "Filtrer par entité", description = "Ex : tous les accès à un bénévole donné (`entiteCible=Benevole`, `entiteId=uuid`).")
    // Entrées pour une entité précise (ex : tous les accès à un bénévole)
    @GetMapping("/entite")
    public ResponseEntity<Page<JournalAuditResponse>> listerParEntite(
            @RequestParam String entiteCible,
            @RequestParam UUID entiteId,
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(journalAuditService.listerParEntite(entiteCible, entiteId, pageable));
    }

    @Operation(summary = "Filtrer par utilisateur", description = "Tous les accès effectués par un utilisateur donné.")
    // Entrées pour un utilisateur donné (qui a accédé à quoi)
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<Page<JournalAuditResponse>> listerParUtilisateur(
            @PathVariable UUID utilisateurId,
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(journalAuditService.listerParUtilisateur(utilisateurId, pageable));
    }
}
