package com.festmanager.service;

import com.festmanager.dto.EvenementRequest;
import com.festmanager.dto.EvenementResponse;
import com.festmanager.entity.Evenement;
import com.festmanager.entity.Utilisateur;
import com.festmanager.entity.enums.RoleUtilisateur;
import com.festmanager.entity.enums.StatutEvenement;
import com.festmanager.mapper.EvenementMapper;
import com.festmanager.repository.EvenementRepository;
import com.festmanager.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvenementService {

    private final EvenementRepository evenementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EvenementMapper evenementMapper;

    @Transactional(readOnly = true)
    public Page<EvenementResponse> listerEvenements(StatutEvenement statut, Pageable pageable) {
        Page<Evenement> evenements = (statut != null)
                ? evenementRepository.findByStatut(statut, pageable)
                : evenementRepository.findAll(pageable);
        return evenements.map(evenementMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EvenementResponse obtenirEvenement(UUID id) {
        return evenementMapper.toResponse(trouverParId(id));
    }

    @Transactional
    public EvenementResponse creerEvenement(EvenementRequest request) {
        validerDates(request);
        Utilisateur organisateur = utilisateurCourant();
        Evenement evenement = evenementMapper.toEntity(request, organisateur);
        return evenementMapper.toResponse(evenementRepository.save(evenement));
    }

    @Transactional
    public EvenementResponse modifierEvenement(UUID id, EvenementRequest request) {
        validerDates(request);
        Evenement evenement = trouverParId(id);
        verifierDroitsModification(evenement);

        evenement.setNom(request.getNom());
        evenement.setDescription(request.getDescription());
        evenement.setDateDebut(request.getDateDebut());
        evenement.setDateFin(request.getDateFin());
        evenement.setLieu(request.getLieu());

        return evenementMapper.toResponse(evenementRepository.save(evenement));
    }

    @Transactional
    public EvenementResponse changerStatut(UUID id, StatutEvenement nouveauStatut) {
        Evenement evenement = trouverParId(id);
        verifierDroitsModification(evenement);
        evenement.setStatut(nouveauStatut);
        return evenementMapper.toResponse(evenementRepository.save(evenement));
    }

    @Transactional
    public void supprimerEvenement(UUID id) {
        Evenement evenement = trouverParId(id);
        verifierDroitsModification(evenement);
        evenementRepository.delete(evenement);
    }

    // --- Méthodes privées ---

    private Evenement trouverParId(UUID id) {
        return evenementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable"));
    }

    private Utilisateur utilisateurCourant() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié"));
    }

    private void verifierDroitsModification(Evenement evenement) {
        Utilisateur courant = utilisateurCourant();
        boolean estAdmin = courant.getRole() == RoleUtilisateur.ADMIN;
        boolean estOrganisateur = evenement.getOrganisateur().getId().equals(courant.getId());
        if (!estAdmin && !estOrganisateur) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        }
    }

    private void validerDates(EvenementRequest request) {
        if (request.getDateFin().isBefore(request.getDateDebut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de fin doit être après la date de début");
        }
    }
}
