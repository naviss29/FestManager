package com.festmanager.repository;

import com.festmanager.entity.Creneau;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreneauRepository extends JpaRepository<Creneau, UUID> {


    @EntityGraph(attributePaths = {"mission"})
    List<Creneau> findByMissionId(UUID missionId);

    // Charge mission + evenement en un seul JOIN pour éviter les lazy loads chaînés
    // dans AffectationService.affecter() (vérification conflits horaires et multi-affectation)
    @Query("SELECT c FROM Creneau c WHERE c.id = :id")
    @EntityGraph(attributePaths = {"mission", "mission.evenement"})
    Optional<Creneau> findByIdWithMissionAndEvenement(@Param("id") UUID id);

    // Recherche les chevauchements horaires pour un bénévole sur un événement donné
    @Query(value = """
        SELECT DISTINCT c.* FROM creneau c
        JOIN mission m ON c.mission_id = m.id
        WHERE m.evenement_id = :evenementId
          AND c.debut < :fin
          AND c.fin > :debut
          AND EXISTS (
            SELECT 1 FROM affectation a
            WHERE a.creneau_id = c.id
              AND a.benevole_id = :benevoleId
              AND a.statut IN ('EN_ATTENTE', 'CONFIRME')
          )
    """, nativeQuery = true)
    List<Creneau> findChevauchements(
            @Param("evenementId") UUID evenementId,
            @Param("benevoleId") UUID benevoleId,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin
    );
}
