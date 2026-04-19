package com.festmanager.controller;

import com.festmanager.dto.UtilisateurResponse;
import com.festmanager.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Administration des comptes utilisateurs.
 * Tous les endpoints nécessitent le rôle ADMIN.
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Utilisateurs (Admin)", description = "Validation et gestion des comptes organisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @Operation(
        summary = "Lister les utilisateurs",
        description = "enAttente=true → uniquement les comptes en attente de validation.")
    @GetMapping
    public ResponseEntity<Page<UtilisateurResponse>> lister(
            @RequestParam(defaultValue = "false") boolean enAttente,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(utilisateurService.lister(enAttente, pageable));
    }

    @Operation(summary = "Valider un compte", description = "Active le compte — l'utilisateur peut désormais se connecter.")
    @PostMapping("/{id}/valider")
    public ResponseEntity<UtilisateurResponse> valider(@PathVariable UUID id) {
        return ResponseEntity.ok(utilisateurService.valider(id));
    }

    @Operation(summary = "Rejeter un compte", description = "Supprime définitivement la demande de compte.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejeter(@PathVariable UUID id) {
        utilisateurService.rejeter(id);
    }
}
