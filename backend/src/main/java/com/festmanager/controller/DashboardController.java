package com.festmanager.controller;

import com.festmanager.dto.DashboardSnapshotResponse;
import com.festmanager.websocket.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Dashboard", description = "Snapshot temps réel des statistiques d'un événement — à combiner avec le WebSocket STOMP /topic/dashboard/{id}")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Snapshot du dashboard", description = "Retourne l'état initial (nb missions, créneaux, bénévoles, taux de remplissage). Le client s'abonne ensuite au topic WebSocket `/topic/dashboard/{evenementId}` pour les mises à jour en temps réel.")
    @GetMapping("/{evenementId}")
    public ResponseEntity<DashboardSnapshotResponse> snapshot(@PathVariable UUID evenementId) {
        return ResponseEntity.ok(dashboardService.snapshot(evenementId));
    }
}
