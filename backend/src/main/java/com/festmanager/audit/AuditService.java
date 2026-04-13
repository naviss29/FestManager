package com.festmanager.audit;

import com.festmanager.entity.JournalAudit;
import com.festmanager.entity.Utilisateur;
import com.festmanager.entity.enums.ActionAudit;
import com.festmanager.repository.JournalAuditRepository;
import com.festmanager.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service RGPD : trace chaque accès ou modification de données personnelles
 * dans le journal d'audit.
 * Les données personnelles ne sont jamais incluses dans le détail du log.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final JournalAuditRepository journalAuditRepository;
    private final UtilisateurRepository utilisateurRepository;

    public void tracer(ActionAudit action, String entiteCible, UUID entiteId, String ipAddress, String detail) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            utilisateurRepository.findByEmail(email).ifPresent(utilisateur -> {
                JournalAudit entree = JournalAudit.builder()
                        .utilisateur(utilisateur)
                        .action(action)
                        .entiteCible(entiteCible)
                        .entiteId(entiteId)
                        .ipAddress(ipAddress)
                        .detail(detail)
                        .build();
                journalAuditRepository.save(entree);
            });
        } catch (Exception e) {
            // On ne bloque jamais la requête métier si l'audit échoue
            log.warn("Échec de l'enregistrement dans le journal d'audit : {}", e.getMessage());
        }
    }

    public void tracer(ActionAudit action, String entiteCible, UUID entiteId) {
        tracer(action, entiteCible, entiteId, null, null);
    }
}
