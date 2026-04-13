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
        SELECT c FROM Creneau c
        JOIN c.mission m
        JOIN Affectation a ON a.creneau = c
        WHERE m.evenement.id = :evenementId
          AND a.benevole.id = :benevoleId
          AND a.statut IN ('EN_ATTENTE', 'CONFIRME')
          AND c.debut < :fin
          AND c.fin > :debut
    """)
    List<Creneau> findChevauchements(
            @Param("evenementId") UUID evenementId,
            @Param("benevoleId") UUID benevoleId,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin
    );
}
