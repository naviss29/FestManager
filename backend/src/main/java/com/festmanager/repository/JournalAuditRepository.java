package com.festmanager.repository;

import com.festmanager.entity.JournalAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository pour le journal d'audit RGPD.
 * Toutes les méthodes retournant des entrées d'audit chargent l'utilisateur associé
 * via @EntityGraph pour éviter le N+1 : sans ça, chaque appel à entry.getUtilisateur()
 * dans la boucle de mapping déclencherait une requête SQL supplémentaire.
 */
@Repository
public interface JournalAuditRepository extends JpaRepository<JournalAudit, UUID> {

    // Recherche les entrées d'audit liées à une entité spécifique (ex: "BENEVOLE" + son UUID)
    // Utilisé pour afficher l'historique des accès à une ressource donnée.
    @EntityGraph(attributePaths = {"utilisateur"})
    Page<JournalAudit> findByEntiteCibleAndEntiteId(String entiteCible, UUID entiteId, Pageable pageable);

    // Recherche toutes les actions effectuées par un utilisateur donné
    // Utilisé pour l'audit de traçabilité par utilisateur (conformité RGPD Art. 5).
    @EntityGraph(attributePaths = {"utilisateur"})
    Page<JournalAudit> findByUtilisateurId(UUID utilisateurId, Pageable pageable);
}
