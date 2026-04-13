package com.festmanager.controller;

import com.festmanager.dto.AffectationRequest;
import com.festmanager.dto.AffectationResponse;
import com.festmanager.entity.enums.StatutAffectation;
import com.festmanager.service.AffectationService;
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
public class AffectationController {

    private final AffectationService affectationService;

    // Affectations d'un créneau
    @GetMapping("/api/creneaux/{creneauId}/affectations")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<List<AffectationResponse>> listerParCreneau(@PathVariable UUID creneauId) {
        return ResponseEntity.ok(affectationService.listerParCreneau(creneauId));
    }

    // Affectations d'un bénévole
    @GetMapping("/api/benevoles/{benevoleId}/affectations")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<List<AffectationResponse>> listerParBenevole(@PathVariable UUID benevoleId) {
        return ResponseEntity.ok(affectationService.listerParBenevole(benevoleId));
    }

    @GetMapping("/api/affectations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<AffectationResponse> obtenir(@PathVariable UUID id) {
        return ResponseEntity.ok(affectationService.obtenir(id));
    }

    // Création : contrôle des conflits horaires intégré dans le service
    @PostMapping("/api/affectations")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<AffectationResponse> affecter(@Valid @RequestBody AffectationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(affectationService.affecter(request));
    }

    // Changement de statut : CONFIRME, REFUSE, ANNULE
    @PatchMapping("/api/affectations/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<AffectationResponse> changerStatut(
            @PathVariable UUID id,
            @RequestParam StatutAffectation statut) {
        return ResponseEntity.ok(affectationService.changerStatut(id, statut));
    }

    @DeleteMapping("/api/affectations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        affectationService.supprimer(id);
    }
}
