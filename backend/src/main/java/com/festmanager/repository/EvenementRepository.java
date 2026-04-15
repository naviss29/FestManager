package com.festmanager.repository;

import com.festmanager.entity.Evenement;
import com.festmanager.entity.enums.StatutEvenement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, UUID> {

    @Override
    @EntityGraph(attributePaths = {"organisateur"})
    Page<Evenement> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"organisateur"})
    Page<Evenement> findByStatut(StatutEvenement statut, Pageable pageable);

    @EntityGraph(attributePaths = {"organisateur"})
    Page<Evenement> findByOrganisateurId(UUID organisateurId, Pageable pageable);
}
