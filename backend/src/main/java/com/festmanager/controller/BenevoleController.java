package com.festmanager.controller;

import com.festmanager.dto.BenevoleInvitationRequest;
import com.festmanager.dto.BenevoleRequest;
import com.festmanager.dto.BenevoleResponse;
import com.festmanager.entity.enums.StatutCompteBenevole;
import com.festmanager.service.BenevoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/benevoles")
@RequiredArgsConstructor
@Tag(name = "Bénévoles", description = "Gestion des bénévoles — 3 flux d'inscription, droits RGPD intégrés")
public class BenevoleController {

    private final BenevoleService benevoleService;

    // --- Lecture ---

    @Operation(summary = "Lister les bénévoles", description = "Filtrable par statut. Tracé dans le journal d'audit RGPD.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<Page<BenevoleResponse>> lister(
            @RequestParam(required = false) StatutCompteBenevole statut,
            @PageableDefault(size = 20, sort = "nom") Pageable pageable,
            HttpServletRequest request) {
        return ResponseEntity.ok(benevoleService.listerBenevoles(statut, pageable, request.getRemoteAddr()));
    }

    @Operation(summary = "Obtenir un bénévole")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<BenevoleResponse> obtenir(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(benevoleService.obtenirBenevole(id, request.getRemoteAddr()));
    }

    // --- Flux 1 : Inscription libre (public) ---

    @Operation(summary = "Inscription libre (public)", description = "Endpoint public — aucun token requis. Le bénévole s'inscrit lui-même.")
    @SecurityRequirements
    @PostMapping("/inscription")
    public ResponseEntity<BenevoleResponse> inscrire(@Valid @RequestBody BenevoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(benevoleService.inscrire(request));
    }

    // --- Flux 2 : Création manuelle ---

    @Operation(summary = "Créer un bénévole manuellement")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<BenevoleResponse> creer(
            @Valid @RequestBody BenevoleRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(benevoleService.creerManuellement(request, httpRequest.getRemoteAddr()));
    }

    // --- Flux 3 : Invitation par email ---

    @Operation(summary = "Inviter un bénévole par email", description = "Envoie un email d'invitation SMTP de manière asynchrone.")
    @PostMapping("/invitation")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<BenevoleResponse> inviter(
            @Valid @RequestBody BenevoleInvitationRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(benevoleService.inviter(request, httpRequest.getRemoteAddr()));
    }

    // --- Modification ---

    @Operation(summary = "Modifier un bénévole")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<BenevoleResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody BenevoleRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(benevoleService.modifier(id, request, httpRequest.getRemoteAddr()));
    }

    // --- Photo de profil ---

    @Operation(summary = "Uploader la photo d'un bénévole", description = "Remplace la photo existante. Formats acceptés : JPEG, PNG, WEBP.")
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<BenevoleResponse> uploadPhoto(
            @PathVariable UUID id,
            @RequestParam("fichier") MultipartFile fichier,
            HttpServletRequest request) {
        return ResponseEntity.ok(benevoleService.sauvegarderPhoto(id, fichier, request.getRemoteAddr()));
    }

    // --- RGPD : Export des données (Art. 15) ---

    @Operation(summary = "Exporter les données d'un bénévole (RGPD Art. 15)", description = "Retourne un JSON structuré de toutes les données personnelles.")
    @GetMapping("/{id}/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<Map<String, Object>> exporter(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(benevoleService.exporterDonnees(id, request.getRemoteAddr()));
    }

    // --- RGPD : Anonymisation (Art. 17) ---

    @Operation(summary = "Anonymiser un bénévole (RGPD Art. 17)", description = "Remplace les données personnelles par des valeurs anonymes. Irréversible.")
    @PostMapping("/{id}/anonymiser")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void anonymiser(@PathVariable UUID id, HttpServletRequest request) {
        benevoleService.anonymiser(id, request.getRemoteAddr());
    }
}
