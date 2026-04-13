package com.festmanager.controller;

import com.festmanager.dto.CreneauRequest;
import com.festmanager.dto.CreneauResponse;
import com.festmanager.dto.MissionRequest;
import com.festmanager.dto.MissionResponse;
import com.festmanager.entity.enums.CategorieMission;
import com.festmanager.service.CreneauService;
import com.festmanager.service.MissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;
    private final CreneauService creneauService;

    // --- Missions ---

    @GetMapping("/api/evenements/{evenementId}/missions")
    public ResponseEntity<Page<MissionResponse>> listerMissions(
            @PathVariable UUID evenementId,
            @RequestParam(required = false) CategorieMission categorie,
            @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return ResponseEntity.ok(missionService.listerMissions(evenementId, categorie, pageable));
    }

    @GetMapping("/api/missions/{id}")
    public ResponseEntity<MissionResponse> obtenirMission(@PathVariable UUID id) {
        return ResponseEntity.ok(missionService.obtenirMission(id));
    }

    @PostMapping("/api/evenements/{evenementId}/missions")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    public ResponseEntity<MissionResponse> creerMission(
            @PathVariable UUID evenementId,
            @Valid @RequestBody MissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(missionService.creerMission(evenementId, request));
    }

    @PutMapping("/api/missions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    public ResponseEntity<MissionResponse> modifierMission(
            @PathVariable UUID id,
            @Valid @RequestBody MissionRequest request) {
        return ResponseEntity.ok(missionService.modifierMission(id, request));
    }

    @DeleteMapping("/api/missions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerMission(@PathVariable UUID id) {
        missionService.supprimerMission(id);
    }

    // --- Créneaux ---

    @GetMapping("/api/missions/{missionId}/creneaux")
    public ResponseEntity<List<CreneauResponse>> listerCreneaux(@PathVariable UUID missionId) {
        return ResponseEntity.ok(creneauService.listerCreneaux(missionId));
    }

    @GetMapping("/api/creneaux/{id}")
    public ResponseEntity<CreneauResponse> obtenirCreneau(@PathVariable UUID id) {
        return ResponseEntity.ok(creneauService.obtenirCreneau(id));
    }

    @PostMapping("/api/missions/{missionId}/creneaux")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    public ResponseEntity<CreneauResponse> creerCreneau(
            @PathVariable UUID missionId,
            @Valid @RequestBody CreneauRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(creneauService.creerCreneau(missionId, request));
    }

    @PutMapping("/api/creneaux/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    public ResponseEntity<CreneauResponse> modifierCreneau(
            @PathVariable UUID id,
            @Valid @RequestBody CreneauRequest request) {
        return ResponseEntity.ok(creneauService.modifierCreneau(id, request));
    }

    @DeleteMapping("/api/creneaux/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerCreneau(@PathVariable UUID id) {
        creneauService.supprimerCreneau(id);
    }
}
