package com.festmanager.repository;

import com.festmanager.entity.Accreditation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccreditationRepository extends JpaRepository<Accreditation, UUID> {

    @EntityGraph(attributePaths = {"benevole", "evenement"})
    List<Accreditation> findByEvenementId(UUID evenementId);

    @EntityGraph(attributePaths = {"benevole", "evenement"})
    List<Accreditation> findByBenevoleId(UUID benevoleId);

    Optional<Accreditation> findByBenevoleIdAndEvenementId(UUID benevoleId, UUID evenementId);

    boolean existsByBenevoleIdAndEvenementId(UUID benevoleId, UUID evenementId);
}
