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

    // @EntityGraph charge en une seule requête SQL (JOIN) toute la chaîne
    // benevole → creneau → mission → evenement, évitant le problème N+1
    // (sans ça, Hibernate ferait 1 requête par association accédée dans la boucle).
    // Charge toute la chaîne d'associations en un seul JOIN
    // Utilisé par trouverParId() pour éviter les lazy loads dans le mapper et les notifications WebSocket
    @Query("SELECT a FROM Affectation a WHERE a.id = :id")
    @EntityGraph(attributePaths = {"benevole", "creneau", "creneau.mission", "creneau.mission.evenement"})
    Optional<Affectation> findByIdWithAssociations(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"benevole", "creneau", "creneau.mission", "creneau.mission.evenement"})
    List<Affectation> findByBenevoleId(UUID benevoleId);

    @EntityGraph(attributePaths = {"benevole", "creneau", "creneau.mission", "creneau.mission.evenement"})
    List<Affectation> findByCreneauId(UUID creneauId);

    Optional<Affectation> findByBenevoleIdAndCreneauId(UUID benevoleId, UUID creneauId);

    // Compte le nombre de bénévoles confirmés sur un créneau donné (utilisé dans le mapper et les vérifications de capacité)
    int countByCreneauIdAndStatut(UUID creneauId, StatutAffectation statut);

    boolean existsByBenevoleIdAndCreneauId(UUID benevoleId, UUID creneauId);

    // --- Requêtes export ---

    // JPQL avec JOIN FETCH : charge benevole, creneau et mission en une seule requête.
    // Évite le N+1 lors de l'export CSV/PDF où l'on accède à a.getBenevole() et a.getCreneau().getMission()
    // pour chaque affectation dans la boucle d'export.
    // (Anciennement une requête native sans fetch — déclenchait N requêtes supplémentaires.)
    @Query("SELECT a FROM Affectation a JOIN FETCH a.benevole JOIN FETCH a.creneau c JOIN FETCH c.mission WHERE c.mission.evenement.id = :evenementId ORDER BY c.mission.nom, c.debut, a.benevole.nom")
    List<Affectation> findParEvenementPourExport(@Param("evenementId") UUID evenementId);

    // Retourne le nombre d'affectations par créneau en une seule requête groupée.
    // Utilisé dans CreneauService.listerCreneaux() pour éviter le N+1 :
    // au lieu de faire COUNT(*) pour chaque créneau individuellement, on récupère
    // tous les comptes d'un coup et on les mappe côté Java (Map<creneauId, count>).
    // Retourne des Object[] : [0] = UUID du créneau, [1] = Long count.
    @Query("SELECT a.creneau.id, COUNT(a) FROM Affectation a WHERE a.creneau.id IN :creneauIds AND a.statut = :statut GROUP BY a.creneau.id")
    List<Object[]> countGroupedByCreneauIds(@Param("creneauIds") List<UUID> creneauIds, @Param("statut") StatutAffectation statut);

    // --- Requêtes dashboard ---

    // Compte les affectations d'un événement par statut (requête native — traverse mission → creneau → affectation)
    @Query(value = "SELECT COUNT(a.id) FROM affectation a JOIN creneau c ON a.creneau_id = c.id JOIN mission m ON c.mission_id = m.id WHERE m.evenement_id = :evenementId AND a.statut = :statut", nativeQuery = true)
    long countParEvenementEtStatut(@Param("evenementId") UUID evenementId, @Param("statut") String statut);

    // Compte les bénévoles distincts engagés sur un événement (un même bénévole sur plusieurs créneaux ne compte qu'une fois)
    @Query(value = "SELECT COUNT(DISTINCT a.benevole_id) FROM affectation a JOIN creneau c ON a.creneau_id = c.id JOIN mission m ON c.mission_id = m.id WHERE m.evenement_id = :evenementId", nativeQuery = true)
    long countBenevolesDistinctsParEvenement(@Param("evenementId") UUID evenementId);

    // Compte les affectations d'une mission par statut (utilisé dans le dashboard pour le taux de remplissage par mission)
    @Query(value = "SELECT COUNT(a.id) FROM affectation a JOIN creneau c ON a.creneau_id = c.id WHERE c.mission_id = :missionId AND a.statut = :statut", nativeQuery = true)
    long countParMissionEtStatut(@Param("missionId") UUID missionId, @Param("statut") String statut);

    // Retourne le compte d'affectations par mission en une seule requête GROUP BY.
    // Remplace N appels à countParMissionEtStatut() dans DashboardService.snapshot()
    // pour éviter le problème N+1 (1 requête COUNT par mission → 1 requête groupée).
    // Retourne des Object[] : [0] = UUID de la mission, [1] = Long count.
    @Query("SELECT c.mission.id, COUNT(a) FROM Affectation a JOIN a.creneau c WHERE c.mission.id IN :missionIds AND a.statut = :statut GROUP BY c.mission.id")
    List<Object[]> countParMissionsGrouped(@Param("missionIds") List<UUID> missionIds, @Param("statut") StatutAffectation statut);
}
