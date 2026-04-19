package com.festmanager.controller;

import com.festmanager.dto.AccreditationRequest;
import com.festmanager.dto.AccreditationResponse;
import com.festmanager.service.AccreditationService;
import com.festmanager.service.BadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accreditations")
@RequiredArgsConstructor
@Tag(name = "Accréditations", description = "Génération d'accréditations avec QR code (ZXing PNG 300×300)")
public class AccreditationController {

    private final AccreditationService accreditationService;
    private final BadgeService badgeService;

    @Operation(summary = "Créer une accréditation", description = "Génère automatiquement un QR code au format PNG encodé en base64.")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    public ResponseEntity<AccreditationResponse> creer(@Valid @RequestBody AccreditationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accreditationService.creer(request));
    }

    @Operation(summary = "Obtenir une accréditation")
    @GetMapping("/{id}")
    public ResponseEntity<AccreditationResponse> obtenir(@PathVariable UUID id) {
        return ResponseEntity.ok(accreditationService.obtenir(id));
    }

    @Operation(summary = "Télécharger le QR code (PNG)", description = "Retourne l'image PNG du QR code — utilisable directement dans un `<img src='...'>`.")
    // Retourne l'image QR code au format PNG — utilisable directement dans <img src="...">
    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> obtenirQrImage(@PathVariable UUID id) {
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(accreditationService.obtenirImageQr(id));
    }

    @Operation(summary = "Lister les accréditations d'un événement")
    @GetMapping("/evenement/{evenementId}")
    public ResponseEntity<List<AccreditationResponse>> listerParEvenement(@PathVariable UUID evenementId) {
        return ResponseEntity.ok(accreditationService.listerParEvenement(evenementId));
    }

    @Operation(summary = "Lister les accréditations d'un bénévole")
    @GetMapping("/benevole/{benevoleId}")
    public ResponseEntity<List<AccreditationResponse>> listerParBenevole(@PathVariable UUID benevoleId) {
        return ResponseEntity.ok(accreditationService.listerParBenevole(benevoleId));
    }

    @Operation(
        summary = "Télécharger le badge PDF",
        description = "Retourne un PDF A6 paysage avec prénom/nom, type, zones, QR code et dates de validité."
    )
    @GetMapping(value = "/{id}/badge", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> telechargerBadge(@PathVariable UUID id) {
        byte[] pdf = badgeService.genererBadge(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
            ContentDisposition.attachment().filename("badge.pdf").build()
        );
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @Operation(
        summary = "Télécharger tous les badges (ZIP)",
        description = "Génère un fichier ZIP contenant un badge PDF par accréditation de l'événement."
    )
    @GetMapping(value = "/evenement/{evenementId}/badges", produces = "application/zip")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    public ResponseEntity<byte[]> telechargerBadgesZip(@PathVariable UUID evenementId) {
        byte[] zip = badgeService.genererBadgesZip(evenementId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
            ContentDisposition.attachment().filename("badges.zip").build()
        );
        return ResponseEntity.ok().headers(headers).body(zip);
    }

    @Operation(summary = "Supprimer une accréditation")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        accreditationService.supprimer(id);
    }
}
