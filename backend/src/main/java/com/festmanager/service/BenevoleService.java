package com.festmanager.service;

import com.festmanager.audit.AuditService;
import com.festmanager.dto.BenevoleInvitationRequest;
import com.festmanager.dto.BenevoleRequest;
import com.festmanager.dto.BenevoleResponse;
import com.festmanager.entity.Benevole;
import com.festmanager.entity.enums.ActionAudit;
import com.festmanager.entity.enums.StatutCompteBenevole;
import com.festmanager.mapper.BenevoleMapper;
import com.festmanager.repository.BenevoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BenevoleService {

    private static final String ENTITE = "BENEVOLE";
    private static final String VERSION_CGU = "1.0";

    private final BenevoleRepository benevoleRepository;
    private final BenevoleMapper benevoleMapper;
    private final AuditService auditService;

    // --- Lecture ---

    public Page<BenevoleResponse> listerBenevoles(StatutCompteBenevole statut, Pageable pageable, String ip) {
        Page<Benevole> benevoles = (statut != null)
                ? benevoleRepository.findByStatutCompte(statut, pageable)
                : benevoleRepository.findAll(pageable);
        return benevoles.map(b -> {
            auditService.tracer(ActionAudit.LECTURE, ENTITE, b.getId(), ip, null);
            return benevoleMapper.toResponse(b);
        });
    }

    public BenevoleResponse obtenirBenevole(UUID id, String ip) {
        Benevole benevole = trouverParId(id);
        auditService.tracer(ActionAudit.LECTURE, ENTITE, id, ip, null);
        return benevoleMapper.toResponse(benevole);
    }

    // --- Flux 1 : Inscription libre (public) ---

    @Transactional
    public BenevoleResponse inscrire(BenevoleRequest request) {
        verifierEmailDisponible(request.getEmail());
        Benevole benevole = benevoleMapper.toEntity(request, StatutCompteBenevole.INSCRIT);
        Benevole sauvegarde = benevoleRepository.save(benevole);
        auditService.tracer(ActionAudit.CREATION, ENTITE, sauvegarde.getId(), null, "inscription libre");
        return benevoleMapper.toResponse(sauvegarde);
    }

    // --- Flux 2 : Création manuelle par l'organisateur ---

    @Transactional
    public BenevoleResponse creerManuellement(BenevoleRequest request, String ip) {
        verifierEmailDisponible(request.getEmail());
        Benevole benevole = benevoleMapper.toEntity(request, StatutCompteBenevole.VALIDE);
        Benevole sauvegarde = benevoleRepository.save(benevole);
        auditService.tracer(ActionAudit.CREATION, ENTITE, sauvegarde.getId(), ip, "création manuelle");
        return benevoleMapper.toResponse(sauvegarde);
    }

    // --- Flux 3 : Invitation par email ---

    @Transactional
    public BenevoleResponse inviter(BenevoleInvitationRequest request, String ip) {
        verifierEmailDisponible(request.getEmail());
        Benevole benevole = Benevole.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .statutCompte(StatutCompteBenevole.INVITE)
                // Consentement sera recueilli lors de l'activation du compte
                .consentementRgpd(false)
                .dateConsentement(LocalDateTime.now())
                .versionCgu(VERSION_CGU)
                .build();
        Benevole sauvegarde = benevoleRepository.save(benevole);
        auditService.tracer(ActionAudit.CREATION, ENTITE, sauvegarde.getId(), ip, "invitation email");
        return benevoleMapper.toResponse(sauvegarde);
    }

    // --- Modification ---

    @Transactional
    public BenevoleResponse modifier(UUID id, BenevoleRequest request, String ip) {
        Benevole benevole = trouverParId(id);
        if (!benevole.getEmail().equals(request.getEmail())) {
            verifierEmailDisponible(request.getEmail());
        }
        benevole.setNom(request.getNom());
        benevole.setPrenom(request.getPrenom());
        benevole.setEmail(request.getEmail());
        benevole.setTelephone(request.getTelephone());
        benevole.setCompetences(request.getCompetences());
        benevole.setTailleTshirt(request.getTailleTshirt());
        benevole.setDateNaissance(request.getDateNaissance());
        benevole.setDisponibilites(request.getDisponibilites());

        Benevole sauvegarde = benevoleRepository.save(benevole);
        auditService.tracer(ActionAudit.MODIFICATION, ENTITE, id, ip, "mise à jour profil");
        return benevoleMapper.toResponse(sauvegarde);
    }

    // --- RGPD : Export des données (Art. 15) ---

    @Transactional(readOnly = true)
    public Map<String, Object> exporterDonnees(UUID id, String ip) {
        Benevole b = trouverParId(id);
        auditService.tracer(ActionAudit.EXPORT, ENTITE, id, ip, "export RGPD Art.15");
        Map<String, Object> export = new LinkedHashMap<>();
        export.put("id", b.getId());
        export.put("nom", b.getNom());
        export.put("prenom", b.getPrenom());
        export.put("email", b.getEmail());
        export.put("telephone", b.getTelephone() != null ? b.getTelephone() : "");
        export.put("competences", b.getCompetences() != null ? b.getCompetences() : "");
        export.put("tailleTshirt", b.getTailleTshirt() != null ? b.getTailleTshirt() : "");
        export.put("dateNaissance", b.getDateNaissance() != null ? b.getDateNaissance() : "");
        export.put("consentementRgpd", b.getConsentementRgpd());
        export.put("dateConsentement", b.getDateConsentement());
        export.put("versionCgu", b.getVersionCgu());
        export.put("createdAt", b.getCreatedAt());
        return export;
    }

    // --- RGPD : Anonymisation (Art. 17) ---

    @Transactional
    public void anonymiser(UUID id, String ip) {
        Benevole benevole = trouverParId(id);
        if (benevole.getStatutCompte() == StatutCompteBenevole.ANONYMISE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce bénévole est déjà anonymisé");
        }
        // Remplacement des données personnelles par des valeurs neutres
        benevole.setNom("ANONYME");
        benevole.setPrenom("ANONYME");
        benevole.setEmail("anonyme-" + id + "@supprime.invalid");
        benevole.setTelephone(null);
        benevole.setCompetences(null);
        benevole.setTailleTshirt(null);
        benevole.setDateNaissance(null);
        benevole.setDisponibilites(null);
        benevole.setConsentementRgpd(false);
        benevole.setStatutCompte(StatutCompteBenevole.ANONYMISE);
        benevole.setDateAnonymisation(LocalDateTime.now());

        benevoleRepository.save(benevole);
        auditService.tracer(ActionAudit.ANONYMISATION, ENTITE, id, ip, "anonymisation RGPD Art.17");
    }

    // --- Méthodes privées ---

    private Benevole trouverParId(UUID id) {
        return benevoleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bénévole introuvable"));
    }

    private void verifierEmailDisponible(String email) {
        if (benevoleRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà utilisé");
        }
    }
}
