package com.festmanager.repository;

import com.festmanager.entity.Benevole;
import com.festmanager.entity.enums.StatutCompteBenevole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BenevoleRepository extends JpaRepository<Benevole, UUID> {

    Optional<Benevole> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Benevole> findByStatutCompte(StatutCompteBenevole statut, Pageable pageable);

    /**
     * Retourne les bénévoles éligibles à l'anonymisation automatique :
     * - non encore anonymisés
     * - dont le dernier événement (date_fin) est antérieur à la date limite fournie
     * - ou qui n'ont jamais eu d'affectation et dont le compte est créé avant la date limite
     *
     * Règle RGPD : anonymisation 3 ans après le dernier événement (Art. 17).
     */
    @Query(value = """
        SELECT DISTINCT b.* FROM benevole b
        WHERE b.statut_compte <> 'ANONYMISE'
          AND (
            (SELECT MAX(e.date_fin) FROM affectation a
             JOIN creneau c ON a.creneau_id = c.id
             JOIN mission m ON c.mission_id = m.id
             JOIN evenement e ON m.evenement_id = e.id
             WHERE a.benevole_id = b.id) < :dateLimite
            OR
            (NOT EXISTS (SELECT 1 FROM affectation a WHERE a.benevole_id = b.id)
             AND b.created_at < :dateLimiteCreation)
          )
    """, nativeQuery = true)
    List<Benevole> findEligiblesAnonymisation(
        @Param("dateLimite") LocalDate dateLimite,
        @Param("dateLimiteCreation") java.time.LocalDateTime dateLimiteCreation
    );
}
