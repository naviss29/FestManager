package com.festmanager.repository;

import com.festmanager.entity.Affectation;
import com.festmanager.entity.enums.StatutAffectation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, UUID> {

    @EntityGraph(attributePaths = {"benevole", "creneau", "creneau.mission", "creneau.mission.evenement"})
    List<Affectation> findByBenevoleId(UUID benevoleId);

    @EntityGraph(attributePaths = {"benevole", "creneau", "creneau.mission", "creneau.mission.evenement"})
    List<Affectation> findByCreneauId(UUID creneauId);

    Optional<Affectation> findByBenevoleIdAndCreneauId(UUID benevoleId, UUID creneauId);

    int countByCreneauIdAndStatut(UUID creneauId, StatutAffectation statut);

    boolean existsByBenevoleIdAndCreneauId(UUID benevoleId, UUID creneauId);

    // --- Requêtes export ---

    @Query(value = """
        SELECT a.* FROM affectation a
        JOIN creneau c ON a.creneau_id = c.id
        JOIN mission m ON c.mission_id = m.id
        JOIN benevole b ON a.benevole_id = b.id
        WHERE m.evenement_id = :evenementId
        ORDER BY m.nom, c.debut, b.nom
    """, nativeQuery = true)
    List<Affectation> findParEvenementPourExport(@Param("evenementId") UUID evenementId);

    // --- Requêtes dashboard ---

    @Query(value = "SELECT COUNT(a.id) FROM affectation a JOIN creneau c ON a.creneau_id = c.id JOIN mission m ON c.mission_id = m.id WHERE m.evenement_id = :evenementId AND a.statut = :statut", nativeQuery = true)
    long countParEvenementEtStatut(@Param("evenementId") UUID evenementId, @Param("statut") String statut);

    @Query(value = "SELECT COUNT(DISTINCT a.benevole_id) FROM affectation a JOIN creneau c ON a.creneau_id = c.id JOIN mission m ON c.mission_id = m.id WHERE m.evenement_id = :evenementId", nativeQuery = true)
    long countBenevolesDistinctsParEvenement(@Param("evenementId") UUID evenementId);

    @Query(value = "SELECT COUNT(a.id) FROM affectation a JOIN creneau c ON a.creneau_id = c.id WHERE c.mission_id = :missionId AND a.statut = :statut", nativeQuery = true)
    long countParMissionEtStatut(@Param("missionId") UUID missionId, @Param("statut") String statut);
}
