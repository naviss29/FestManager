package com.festmanager.controller;

import com.festmanager.dto.AffectationRequest;
import com.festmanager.dto.AffectationResponse;
import com.festmanager.entity.enums.StatutAffectation;
import com.festmanager.service.AffectationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Affectations", description = "Affectation de bénévoles à des créneaux — contrôle des conflits horaires intégré")
public class AffectationController {

    private final AffectationService affectationService;

    @Operation(summary = "Lister les affectations d'un créneau")
    // Affectations d'un créneau
    @GetMapping("/api/creneaux/{creneauId}/affectations")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<List<AffectationResponse>> listerParCreneau(@PathVariable UUID creneauId) {
        return ResponseEntity.ok(affectationService.listerParCreneau(creneauId));
    }

    @Operation(summary = "Lister les affectations d'un bénévole")
    // Affectations d'un bénévole
    @GetMapping("/api/benevoles/{benevoleId}/affectations")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<List<AffectationResponse>> listerParBenevole(@PathVariable UUID benevoleId) {
        return ResponseEntity.ok(affectationService.listerParBenevole(benevoleId));
    }

    @Operation(summary = "Obtenir une affectation")
    @GetMapping("/api/affectations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<AffectationResponse> obtenir(@PathVariable UUID id) {
        return ResponseEntity.ok(affectationService.obtenir(id));
    }

    @Operation(summary = "Affecter un bénévole à un créneau", description = "Vérifie les conflits horaires avant d'affecter. Retourne 409 en cas de conflit.")
    // Création : contrôle des conflits horaires intégré dans le service
    @PostMapping("/api/affectations")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<AffectationResponse> affecter(@Valid @RequestBody AffectationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(affectationService.affecter(request));
    }

    @Operation(summary = "Changer le statut d'une affectation", description = "Valeurs : CONFIRME, REFUSE, ANNULE. Un email de confirmation est envoyé si CONFIRME.")
    // Changement de statut : CONFIRME, REFUSE, ANNULE
    @PatchMapping("/api/affectations/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<AffectationResponse> changerStatut(
            @PathVariable UUID id,
            @RequestParam StatutAffectation statut) {
        return ResponseEntity.ok(affectationService.changerStatut(id, statut));
    }

    @Operation(summary = "Supprimer une affectation")
    @DeleteMapping("/api/affectations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        affectationService.supprimer(id);
    }
}
