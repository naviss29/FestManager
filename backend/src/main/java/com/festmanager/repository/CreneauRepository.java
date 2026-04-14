package com.festmanager.repository;

import com.festmanager.entity.Creneau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CreneauRepository extends JpaRepository<Creneau, UUID> {

    List<Creneau> findByMissionId(UUID missionId);

    // Recherche les chevauchements horaires pour un bénévole sur un événement donné
    @Query("""
        SELECT DISTINCT c FROM Creneau c
        JOIN c.mission m
        WHERE m.evenement.id = :evenementId
          AND c.debut < :fin
          AND c.fin > :debut
          AND EXISTS (
            SELECT a FROM Affectation a
            WHERE a.creneau = c
              AND a.benevole.id = :benevoleId
              AND a.statut IN ('EN_ATTENTE', 'CONFIRME')
          )
    """)
    List<Creneau> findChevauchements(
            @Param("evenementId") UUID evenementId,
            @Param("benevoleId") UUID benevoleId,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin
    );
}
