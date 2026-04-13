package com.festmanager.repository;

import com.festmanager.entity.Affectation;
import com.festmanager.entity.enums.StatutAffectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, UUID> {

    List<Affectation> findByBenevoleId(UUID benevoleId);

    List<Affectation> findByCreneauId(UUID creneauId);

    Optional<Affectation> findByBenevoleIdAndCreneauId(UUID benevoleId, UUID creneauId);

    int countByCreneauIdAndStatut(UUID creneauId, StatutAffectation statut);

    boolean existsByBenevoleIdAndCreneauId(UUID benevoleId, UUID creneauId);

    // --- Requêtes export ---

    @Query("""
        SELECT a FROM Affectation a
        JOIN FETCH a.benevole
        JOIN FETCH a.creneau c
        JOIN FETCH c.mission m
        JOIN FETCH m.evenement
        WHERE m.evenement.id = :evenementId
        ORDER BY m.nom, c.debut, a.benevole.nom
    """)
    List<Affectation> findParEvenementPourExport(@Param("evenementId") UUID evenementId);

    // --- Requêtes dashboard ---

    @Query("SELECT COUNT(a) FROM Affectation a WHERE a.creneau.mission.evenement.id = :evenementId AND a.statut = :statut")
    long countParEvenementEtStatut(@Param("evenementId") UUID evenementId, @Param("statut") StatutAffectation statut);

    @Query("SELECT COUNT(DISTINCT a.benevole.id) FROM Affectation a WHERE a.creneau.mission.evenement.id = :evenementId")
    long countBenevolesDistinctsParEvenement(@Param("evenementId") UUID evenementId);

    @Query("SELECT COUNT(a) FROM Affectation a WHERE a.creneau.mission.id = :missionId AND a.statut = :statut")
    long countParMissionEtStatut(@Param("missionId") UUID missionId, @Param("statut") StatutAffectation statut);
}
