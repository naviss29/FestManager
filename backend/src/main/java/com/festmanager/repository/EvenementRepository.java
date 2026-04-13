package com.festmanager.repository;

import com.festmanager.entity.Evenement;
import com.festmanager.entity.enums.StatutEvenement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, UUID> {

    Page<Evenement> findByStatut(StatutEvenement statut, Pageable pageable);

    Page<Evenement> findByOrganisateurId(UUID organisateurId, Pageable pageable);
}
