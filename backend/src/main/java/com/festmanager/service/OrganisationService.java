package com.festmanager.service;

import com.festmanager.dto.OrganisationRequest;
import com.festmanager.dto.OrganisationResponse;
import com.festmanager.entity.Organisation;
import com.festmanager.entity.Utilisateur;
import com.festmanager.entity.enums.RoleUtilisateur;
import com.festmanager.entity.enums.TypeOrganisation;
import com.festmanager.mapper.OrganisationMapper;
import com.festmanager.repository.OrganisationRepository;
import com.festmanager.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final OrganisationMapper organisationMapper;

    @Transactional(readOnly = true)
    public Page<OrganisationResponse> listerOrganisations(TypeOrganisation type, Pageable pageable) {
        Utilisateur courant = utilisateurCourant();

        // Le référent organisation ne voit que la sienne
        if (courant.getRole() == RoleUtilisateur.REFERENT_ORGANISATION) {
            if (courant.getOrganisation() == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Aucune organisation associée");
            }
            OrganisationResponse response = organisationMapper.toResponse(courant.getOrganisation());
            return new PageImpl<>(java.util.List.of(response), pageable, 1);
        }

        Page<Organisation> organisations = (type != null)
                ? organisationRepository.findByType(type, pageable)
                : organisationRepository.findAll(pageable);
        return organisations.map(organisationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrganisationResponse obtenirOrganisation(UUID id) {
        Utilisateur courant = utilisateurCourant();

        // Le référent ne peut accéder qu'à sa propre organisation
        if (courant.getRole() == RoleUtilisateur.REFERENT_ORGANISATION) {
            verifierAccesReferent(courant, id);
        }

        return organisationMapper.toResponse(trouverParId(id));
    }

    @Transactional
    public OrganisationResponse creerOrganisation(OrganisationRequest request) {
        if (organisationRepository.existsByEmailContact(request.getEmailContact())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une organisation avec cet email existe déjà");
        }
        Organisation organisation = organisationMapper.toEntity(request);
        return organisationMapper.toResponse(organisationRepository.save(organisation));
    }

    @Transactional
    public OrganisationResponse modifierOrganisation(UUID id, OrganisationRequest request) {
        Utilisateur courant = utilisateurCourant();
        Organisation organisation = trouverParId(id);

        // Le référent peut modifier uniquement son organisation
        if (courant.getRole() == RoleUtilisateur.REFERENT_ORGANISATION) {
            verifierAccesReferent(courant, id);
        }

        organisation.setNom(request.getNom());
        organisation.setType(request.getType());
        organisation.setSiret(request.getSiret());
        organisation.setEmailContact(request.getEmailContact());
        organisation.setTelephoneContact(request.getTelephoneContact());
        organisation.setAdresse(request.getAdresse());

        return organisationMapper.toResponse(organisationRepository.save(organisation));
    }

    @Transactional
    public void supprimerOrganisation(UUID id) {
        organisationRepository.delete(trouverParId(id));
    }

    // --- Méthodes privées ---

    private Organisation trouverParId(UUID id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organisation introuvable"));
    }

    private Utilisateur utilisateurCourant() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié"));
    }

    private void verifierAccesReferent(Utilisateur courant, UUID organisationId) {
        if (courant.getOrganisation() == null || !courant.getOrganisation().getId().equals(organisationId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès limité à votre organisation");
        }
    }
}
