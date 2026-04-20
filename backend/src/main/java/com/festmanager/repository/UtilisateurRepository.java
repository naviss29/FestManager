package com.festmanager.repository;

import com.festmanager.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Utilisateur> findByActif(boolean actif, Pageable pageable);

    Optional<Utilisateur> findByResetToken(String resetToken);
}
