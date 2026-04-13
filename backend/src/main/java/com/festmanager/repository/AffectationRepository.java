package com.festmanager.repository;

import com.festmanager.entity.Affectation;
import com.festmanager.entity.enums.StatutAffectation;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
