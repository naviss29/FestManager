package com.festmanager.repository;

import com.festmanager.entity.Mission;
import com.festmanager.entity.enums.CategorieMission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MissionRepository extends JpaRepository<Mission, UUID> {

    Page<Mission> findByEvenementId(UUID evenementId, Pageable pageable);

    Page<Mission> findByEvenementIdAndCategorie(UUID evenementId, CategorieMission categorie, Pageable pageable);

    List<Mission> findByEvenementId(UUID evenementId);

    List<Mission> findByOrganisationId(UUID organisationId);

    @Query(value = "SELECT COALESCE(SUM(c.nb_benevoles_requis), 0) FROM creneau c JOIN mission m ON c.mission_id = m.id WHERE m.evenement_id = :evenementId", nativeQuery = true)
    long sumPlacesRequisesByEvenement(@Param("evenementId") UUID evenementId);

    @Query(value = "SELECT COUNT(c.id) FROM creneau c JOIN mission m ON c.mission_id = m.id WHERE m.evenement_id = :evenementId", nativeQuery = true)
    long countCreneauxByEvenement(@Param("evenementId") UUID evenementId);
}
