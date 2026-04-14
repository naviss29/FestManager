package com.festmanager.controller;

import com.festmanager.dto.EvenementRequest;
import com.festmanager.dto.EvenementResponse;
import com.festmanager.entity.enums.StatutEvenement;
import com.festmanager.service.EvenementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/evenements")
@RequiredArgsConstructor
@Tag(name = "Événements", description = "Gestion des événements (festivals, concerts...)")
public class EvenementController {

    private final EvenementService evenementService;

    @Operation(summary = "Lister les événements", description = "Filtrable par statut, paginé.")
    @GetMapping
    public ResponseEntity<Page<EvenementResponse>> lister(
            @RequestParam(required = false) StatutEvenement statut,
            @PageableDefault(size = 20, sort = "dateDebut") Pageable pageable) {
        return ResponseEntity.ok(evenementService.listerEvenements(statut, pageable));
    }

    @Operation(summary = "Obtenir un événement")
    @GetMapping("/{id}")
    public ResponseEntity<EvenementResponse> obtenir(@PathVariable UUID id) {
        return ResponseEntity.ok(evenementService.obtenirEvenement(id));
    }

    @Operation(summary = "Créer un événement")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    public ResponseEntity<EvenementResponse> creer(@Valid @RequestBody EvenementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evenementService.creerEvenement(request));
    }

    @Operation(summary = "Modifier un événement")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    public ResponseEntity<EvenementResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody EvenementRequest request) {
        return ResponseEntity.ok(evenementService.modifierEvenement(id, request));
    }

    @Operation(summary = "Changer le statut d'un événement", description = "Valeurs : BROUILLON, PUBLIE, ARCHIVE")
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    public ResponseEntity<EvenementResponse> changerStatut(
            @PathVariable UUID id,
            @RequestParam StatutEvenement statut) {
        return ResponseEntity.ok(evenementService.changerStatut(id, statut));
    }

    @Operation(summary = "Supprimer un événement")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        evenementService.supprimerEvenement(id);
    }
}
