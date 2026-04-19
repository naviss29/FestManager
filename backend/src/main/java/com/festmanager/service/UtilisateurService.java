package com.festmanager.service;

import com.festmanager.dto.UtilisateurResponse;
import com.festmanager.entity.Utilisateur;
import com.festmanager.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Gestion des comptes utilisateurs (réservé à l'administration).
 * Permet de lister les comptes en attente de validation et de les valider ou rejeter.
 */
@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Liste tous les utilisateurs, ou uniquement ceux en attente (actif = false) selon le filtre.
     *
     * @param enAttente true → uniquement les comptes inactifs, false → tous les comptes
     */
    @Transactional(readOnly = true)
    public Page<UtilisateurResponse> lister(boolean enAttente, Pageable pageable) {
        Page<Utilisateur> utilisateurs = enAttente
                ? utilisateurRepository.findByActif(false, pageable)
                : utilisateurRepository.findAll(pageable);
        return utilisateurs.map(this::toResponse);
    }

    /**
     * Active un compte en attente — le compte peut désormais se connecter.
     */
    @Transactional
    public UtilisateurResponse valider(UUID id) {
        Utilisateur utilisateur = trouverParId(id);
        if (utilisateur.getActif()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce compte est déjà actif.");
        }
        utilisateur.setActif(true);
        return toResponse(utilisateurRepository.save(utilisateur));
    }

    /**
     * Supprime un compte en attente de validation (rejet de la demande).
     */
    @Transactional
    public void rejeter(UUID id) {
        Utilisateur utilisateur = trouverParId(id);
        if (utilisateur.getActif()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Impossible de rejeter un compte déjà actif. Utilisez la désactivation.");
        }
        utilisateurRepository.delete(utilisateur);
    }

    // ── Helpers privés ────────────────────────────────────────────────────────

    private Utilisateur trouverParId(UUID id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private UtilisateurResponse toResponse(Utilisateur u) {
        UtilisateurResponse r = new UtilisateurResponse();
        r.setId(u.getId());
        r.setEmail(u.getEmail());
        r.setRole(u.getRole());
        r.setActif(u.getActif());
        r.setCreatedAt(u.getCreatedAt());
        r.setDerniereConnexion(u.getDerniereConnexion());
        return r;
    }
}
