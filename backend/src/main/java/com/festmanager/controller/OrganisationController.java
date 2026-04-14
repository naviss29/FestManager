package com.festmanager.controller;

import com.festmanager.dto.OrganisationRequest;
import com.festmanager.dto.OrganisationResponse;
import com.festmanager.entity.enums.TypeOrganisation;
import com.festmanager.service.OrganisationService;
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
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
@Tag(name = "Organisations", description = "Organisations prestataires et partenaires liées aux événements")
public class OrganisationController {

    private final OrganisationService organisationService;

    @Operation(summary = "Lister les organisations", description = "Filtrable par type.")
    // Tous les rôles authentifiés peuvent lister (filtrage côté service pour le référent)
    @GetMapping
    public ResponseEntity<Page<OrganisationResponse>> lister(
            @RequestParam(required = false) TypeOrganisation type,
            @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return ResponseEntity.ok(organisationService.listerOrganisations(type, pageable));
    }

    @Operation(summary = "Obtenir une organisation")
    // Tous les rôles authentifiés peuvent consulter (contrôle d'accès pour le référent côté service)
    @GetMapping("/{id}")
    public ResponseEntity<OrganisationResponse> obtenir(@PathVariable UUID id) {
        return ResponseEntity.ok(organisationService.obtenirOrganisation(id));
    }

    @Operation(summary = "Créer une organisation")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    public ResponseEntity<OrganisationResponse> creer(@Valid @RequestBody OrganisationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organisationService.creerOrganisation(request));
    }

    @Operation(summary = "Modifier une organisation")
    // ADMIN, ORGANISATEUR et REFERENT_ORGANISATION peuvent modifier (le référent uniquement la sienne)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')")
    public ResponseEntity<OrganisationResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody OrganisationRequest request) {
        return ResponseEntity.ok(organisationService.modifierOrganisation(id, request));
    }

    @Operation(summary = "Supprimer une organisation")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        organisationService.supprimerOrganisation(id);
    }
}
