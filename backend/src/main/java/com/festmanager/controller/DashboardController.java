package com.festmanager.controller;

import com.festmanager.dto.DashboardSnapshotResponse;
import com.festmanager.websocket.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Retourne le snapshot initial du dashboard pour un événement.
     * Le client charge cet état puis s'abonne au topic WebSocket
     * /topic/dashboard/{evenementId} pour les mises à jour en temps réel.
     */
    @GetMapping("/{evenementId}")
    public ResponseEntity<DashboardSnapshotResponse> snapshot(@PathVariable UUID evenementId) {
        return ResponseEntity.ok(dashboardService.snapshot(evenementId));
    }
}
