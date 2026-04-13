package com.festmanager.controller;

import com.festmanager.dto.BenevoleInvitationRequest;
import com.festmanager.dto.BenevoleRequest;
import com.festmanager.dto.BenevoleResponse;
import com.festmanager.entity.enums.StatutCompteBenevole;
import com.festmanager.service.BenevoleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/benevoles")
@RequiredArgsConstructor
public class BenevoleController {

    private final BenevoleService benevoleService;

    // --- Lecture ---

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<Page<BenevoleResponse>> lister(
            @RequestParam(required = false) StatutCompteBenevole statut,
            @PageableDefault(size = 20, sort = "nom") Pageable pageable,
            HttpServletRequest request) {
        return ResponseEntity.ok(benevoleService.listerBenevoles(statut, pageable, request.getRemoteAddr()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<BenevoleResponse> obtenir(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(benevoleService.obtenirBenevole(id, request.getRemoteAddr()));
    }

    // --- Flux 1 : Inscription libre (public) ---

    @PostMapping("/inscription")
    public ResponseEntity<BenevoleResponse> inscrire(@Valid @RequestBody BenevoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(benevoleService.inscrire(request));
    }

    // --- Flux 2 : Création manuelle ---

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<BenevoleResponse> creer(
            @Valid @RequestBody BenevoleRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(benevoleService.creerManuellement(request, httpRequest.getRemoteAddr()));
    }

    // --- Flux 3 : Invitation par email ---

    @PostMapping("/invitation")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<BenevoleResponse> inviter(
            @Valid @RequestBody BenevoleInvitationRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(benevoleService.inviter(request, httpRequest.getRemoteAddr()));
    }

    // --- Modification ---

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<BenevoleResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody BenevoleRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(benevoleService.modifier(id, request, httpRequest.getRemoteAddr()));
    }

    // --- RGPD : Export des données (Art. 15) ---

    @GetMapping("/{id}/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<Map<String, Object>> exporter(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(benevoleService.exporterDonnees(id, request.getRemoteAddr()));
    }

    // --- RGPD : Anonymisation (Art. 17) ---

    @PostMapping("/{id}/anonymiser")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void anonymiser(@PathVariable UUID id, HttpServletRequest request) {
        benevoleService.anonymiser(id, request.getRemoteAddr());
    }
}
