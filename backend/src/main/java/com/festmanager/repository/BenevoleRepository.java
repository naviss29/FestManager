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
    @Query("""
        SELECT DISTINCT b FROM Benevole b
        WHERE b.statutCompte <> 'ANONYMISE'
          AND (
            -- A eu des affectations, mais le dernier événement est trop ancien
            (SELECT MAX(e.dateFin) FROM Affectation a
             JOIN a.creneau c JOIN c.mission m JOIN m.evenement e
             WHERE a.benevole = b) < :dateLimite
            OR
            -- N'a jamais eu d'affectation et le compte est trop ancien
            (NOT EXISTS (SELECT a FROM Affectation a WHERE a.benevole = b)
             AND b.createdAt < :dateLimiteCreation)
          )
    """)
    List<Benevole> findEligiblesAnonymisation(
        @Param("dateLimite") LocalDate dateLimite,
        @Param("dateLimiteCreation") java.time.LocalDateTime dateLimiteCreation
    );
}
