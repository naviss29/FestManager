package com.festmanager.controller;

import com.festmanager.dto.AccreditationRequest;
import com.festmanager.dto.AccreditationResponse;
import com.festmanager.service.AccreditationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class AccreditationController {

    private final AccreditationService accreditationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    public ResponseEntity<AccreditationResponse> creer(@Valid @RequestBody AccreditationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accreditationService.creer(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccreditationResponse> obtenir(@PathVariable UUID id) {
        return ResponseEntity.ok(accreditationService.obtenir(id));
    }

    // Retourne l'image QR code au format PNG — utilisable directement dans <img src="...">
    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> obtenirQrImage(@PathVariable UUID id) {
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(accreditationService.obtenirImageQr(id));
    }

    @GetMapping("/evenement/{evenementId}")
    public ResponseEntity<List<AccreditationResponse>> listerParEvenement(@PathVariable UUID evenementId) {
        return ResponseEntity.ok(accreditationService.listerParEvenement(evenementId));
    }

    @GetMapping("/benevole/{benevoleId}")
    public ResponseEntity<List<AccreditationResponse>> listerParBenevole(@PathVariable UUID benevoleId) {
        return ResponseEntity.ok(accreditationService.listerParBenevole(benevoleId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        accreditationService.supprimer(id);
    }
}
