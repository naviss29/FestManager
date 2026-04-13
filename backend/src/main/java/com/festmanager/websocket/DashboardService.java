package com.festmanager.websocket;

import com.festmanager.entity.Affectation;
import com.festmanager.entity.enums.StatutAffectation;
import com.festmanager.repository.AffectationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service WebSocket : pousse les mises à jour du dashboard
 * vers les clients abonnés au topic /topic/dashboard/{evenementId}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AffectationRepository affectationRepository;

    public void notifierAffectation(Affectation affectation, DashboardEvent.TypeEvenement type) {
        try {
            var creneau = affectation.getCreneau();
            var mission = creneau.getMission();
            var evenement = mission.getEvenement();

            int nbAffectes = affectationRepository.countByCreneauIdAndStatut(
                    creneau.getId(), StatutAffectation.CONFIRME);

            DashboardEvent event = new DashboardEvent(
                    type,
                    evenement.getId(),
                    mission.getId(),
                    creneau.getId(),
                    affectation.getBenevole().getId(),
                    mission.getNom(),
                    nbAffectes,
                    creneau.getNbBenevolesRequis(),
                    LocalDateTime.now()
            );

            String destination = "/topic/dashboard/" + evenement.getId();
            messagingTemplate.convertAndSend(destination, event);
            log.debug("Dashboard notifié : {} sur {}", type, destination);

        } catch (Exception e) {
            // Ne jamais bloquer l'opération métier si le WebSocket échoue
            log.warn("Échec notification WebSocket : {}", e.getMessage());
        }
    }
}
