package com.festmanager.repository;

import com.festmanager.entity.Benevole;
import com.festmanager.entity.enums.StatutCompteBenevole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BenevoleRepository extends JpaRepository<Benevole, UUID> {

    Optional<Benevole> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Benevole> findByStatutCompte(StatutCompteBenevole statut, Pageable pageable);
}
