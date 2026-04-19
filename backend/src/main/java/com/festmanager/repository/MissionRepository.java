package com.festmanager.repository;

import com.festmanager.entity.Mission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MissionRepository extends JpaRepository<Mission, UUID> {

    // Version paginée (liste UI) : charge evenement + organisation pour l'affichage des colonnes associées
    @EntityGraph(attributePaths = {"evenement", "organisation"})
    Page<Mission> findByEvenementId(UUID evenementId, Pageable pageable);

    @EntityGraph(attributePaths = {"evenement", "organisation"})
    Page<Mission> findByEvenementIdAndCategorie(UUID evenementId, String categorie, Pageable pageable);

    // Version liste complète (dashboard, stats) : charge aussi les créneaux car on accède à
    // mission.getCreneaux() pour calculer les places requises — sans ce @EntityGraph,
    // chaque appel à getCreneaux() déclencherait une requête SQL supplémentaire (N+1).
    @EntityGraph(attributePaths = {"creneaux", "evenement", "organisation"})
    List<Mission> findByEvenementId(UUID evenementId);

    // Charge evenement + organisation pour les missions d'une organisation (espace référent)
    @EntityGraph(attributePaths = {"evenement", "organisation"})
    List<Mission> findByOrganisationId(UUID organisationId);

    // Somme directement les places requises en base pour éviter de charger toutes les entités en mémoire
    @Query(value = "SELECT COALESCE(SUM(c.nb_benevoles_requis), 0) FROM creneau c JOIN mission m ON c.mission_id = m.id WHERE m.evenement_id = :evenementId", nativeQuery = true)
    long sumPlacesRequisesByEvenement(@Param("evenementId") UUID evenementId);

    // Compte les créneaux en base sans charger les entités (optimisation mémoire pour le dashboard)
    @Query(value = "SELECT COUNT(c.id) FROM creneau c JOIN mission m ON c.mission_id = m.id WHERE m.evenement_id = :evenementId", nativeQuery = true)
    long countCreneauxByEvenement(@Param("evenementId") UUID evenementId);
}
