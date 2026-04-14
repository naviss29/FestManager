package com.festmanager.controller;

import com.festmanager.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/evenements/{evenementId}/export")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
@Tag(name = "Exports", description = "Export du planning en CSV (Apache Commons CSV) ou PDF (OpenPDF) — téléchargement en pièce jointe")
public class ExportController {

    private final ExportService exportService;

    @Operation(summary = "Exporter le planning en CSV", description = "Colonnes : Événement, Mission, Catégorie, Début, Fin, Prénom, Nom, Statut, Commentaire. UTF-8, séparateur `;`.")
    @GetMapping("/csv")
    public ResponseEntity<byte[]> exporterCsv(@PathVariable UUID evenementId) {
        byte[] contenu = exportService.exporterCsv(evenementId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename("planning-" + evenementId + ".csv", StandardCharsets.UTF_8)
            .build());

        return ResponseEntity.ok().headers(headers).body(contenu);
    }

    @Operation(summary = "Exporter le planning en PDF", description = "PDF structuré par mission avec bandeau coloré et lignes zébrées.")
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exporterPdf(@PathVariable UUID evenementId) {
        byte[] contenu = exportService.exporterPdf(evenementId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename("planning-" + evenementId + ".pdf", StandardCharsets.UTF_8)
            .build());

        return ResponseEntity.ok().headers(headers).body(contenu);
    }
}
