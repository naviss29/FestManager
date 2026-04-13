package com.festmanager.repository;

import com.festmanager.entity.Mission;
import com.festmanager.entity.enums.CategorieMission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MissionRepository extends JpaRepository<Mission, UUID> {

    Page<Mission> findByEvenementId(UUID evenementId, Pageable pageable);

    Page<Mission> findByEvenementIdAndCategorie(UUID evenementId, CategorieMission categorie, Pageable pageable);

    List<Mission> findByOrganisationId(UUID organisationId);
}
