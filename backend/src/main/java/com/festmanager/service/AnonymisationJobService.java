package com.festmanager.service;

import com.festmanager.entity.Benevole;
import com.festmanager.repository.BenevoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Job RGPD — anonymisation automatique des bénévoles inactifs (Art. 17).
 *
 * Règle : un bénévole est anonymisé si son dernier événement
 * s'est terminé il y a plus de 3 ans (configurable via DUREE_CONSERVATION_ANNEES).
 * Les bénévoles sans aucune affectation sont anonymisés 3 ans après la création
 * de leur compte (comptes orphelins / invitations non honorées).
 *
 * Le job tourne chaque nuit à 2h du matin.
 * Il est sans effet en profil "dev" (H2 embarqué) grâce au conditional dans application-dev.yml.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnonymisationJobService {

    private static final int DUREE_CONSERVATION_ANNEES = 3;
    // IP fictive utilisée dans le journal d'audit pour les actions automatiques
    private static final String IP_JOB = "job-automatique";

    private final BenevoleRepository benevoleRepository;
    private final BenevoleService benevoleService;

    /**
     * Exécution quotidienne à 2h du matin.
     * Cron : seconde minute heure jour mois jour-semaine
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void lancerPurgeRgpd() {
        LocalDate dateLimite = LocalDate.now().minusYears(DUREE_CONSERVATION_ANNEES);
        LocalDateTime dateLimiteCreation = LocalDateTime.now().minusYears(DUREE_CONSERVATION_ANNEES);

        log.info("[RGPD] Démarrage du job d'anonymisation — date limite événement : {}", dateLimite);

        List<Benevole> eligibles = benevoleRepository.findEligiblesAnonymisation(dateLimite, dateLimiteCreation);

        if (eligibles.isEmpty()) {
            log.info("[RGPD] Aucun bénévole éligible à l'anonymisation.");
            return;
        }

        log.info("[RGPD] {} bénévole(s) éligible(s) à l'anonymisation.", eligibles.size());

        int succes = 0;
        int erreurs = 0;

        for (Benevole benevole : eligibles) {
            try {
                benevoleService.anonymiser(benevole.getId(), IP_JOB);
                succes++;
                log.debug("[RGPD] Bénévole {} anonymisé avec succès.", benevole.getId());
            } catch (Exception e) {
                erreurs++;
                log.warn("[RGPD] Échec anonymisation bénévole {} : {}", benevole.getId(), e.getMessage());
            }
        }

        log.info("[RGPD] Job terminé — {} anonymisé(s), {} erreur(s).", succes, erreurs);
    }
}
