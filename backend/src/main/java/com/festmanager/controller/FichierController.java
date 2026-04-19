package com.festmanager.controller;

import com.festmanager.service.FichierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Sert les fichiers uploadés (photos, bannières) depuis le disque local.
 * Endpoint public (pas d'auth requise) : les noms de fichiers sont des UUIDs,
 * ce qui rend l'énumération impraticable.
 */
@RestController
@RequestMapping("/api/fichiers")
@RequiredArgsConstructor
@Tag(name = "Fichiers", description = "Accès aux fichiers uploadés (photos, bannières)")
public class FichierController {

    private final FichierService fichierService;

    @Operation(
        summary = "Obtenir un fichier",
        description = "Retourne le contenu binaire d'un fichier stocké. Endpoint public.")
    @SecurityRequirements
    @GetMapping("/{sousRep}/{nomFichier}")
    public ResponseEntity<byte[]> servir(
            @PathVariable String sousRep,
            @PathVariable String nomFichier) {

        byte[] contenu = fichierService.charger(sousRep, nomFichier);
        MediaType mediaType = detecterType(nomFichier);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(contenu);
    }

    private MediaType detecterType(String nomFichier) {
        String nom = nomFichier.toLowerCase();
        if (nom.endsWith(".jpg") || nom.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (nom.endsWith(".png"))  return MediaType.IMAGE_PNG;
        if (nom.endsWith(".gif"))  return MediaType.IMAGE_GIF;
        if (nom.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
