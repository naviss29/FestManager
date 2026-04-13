package com.festmanager.controller;

import com.festmanager.service.ExportService;
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
public class ExportController {

    private final ExportService exportService;

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
