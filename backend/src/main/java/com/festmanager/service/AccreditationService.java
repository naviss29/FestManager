package com.festmanager.service;

import com.festmanager.dto.AccreditationRequest;
import com.festmanager.dto.AccreditationResponse;
import com.festmanager.entity.Accreditation;
import com.festmanager.entity.Benevole;
import com.festmanager.entity.Evenement;
import com.festmanager.mapper.AccreditationMapper;
import com.festmanager.repository.AccreditationRepository;
import com.festmanager.repository.BenevoleRepository;
import com.festmanager.repository.EvenementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccreditationService {

    private final AccreditationRepository accreditationRepository;
    private final BenevoleRepository benevoleRepository;
    private final EvenementRepository evenementRepository;
    private final AccreditationMapper accreditationMapper;
    private final QrCodeService qrCodeService;

    /**
     * Crée une accréditation pour un bénévole sur un événement.
     * Génère automatiquement le code QR au format : FESTMANAGER:{uuid}
     */
    @Transactional
    public AccreditationResponse creer(AccreditationRequest request) {
        if (accreditationRepository.existsByBenevoleIdAndEvenementId(request.getBenevoleId(), request.getEvenementId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Ce bénévole possède déjà une accréditation pour cet événement");
        }

        Benevole benevole = benevoleRepository.findById(request.getBenevoleId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bénévole introuvable"));

        Evenement evenement = evenementRepository.findById(request.getEvenementId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable"));

        // Génération de l'identifiant unique encodé dans le QR
        String codeQr = "FESTMANAGER:" + UUID.randomUUID();

        Accreditation accreditation = Accreditation.builder()
            .benevole(benevole)
            .evenement(evenement)
            .type(request.getType())
            .zonesAcces(request.getZonesAcces())
            .dateDebutValidite(request.getDateDebutValidite())
            .dateFinValidite(request.getDateFinValidite())
            .codeQr(codeQr)
            .valide(true)
            .dateEmission(LocalDateTime.now())
            .build();

        return accreditationMapper.toResponse(accreditationRepository.save(accreditation));
    }

    @Transactional(readOnly = true)
    public AccreditationResponse obtenir(UUID id) {
        return accreditationMapper.toResponse(trouverParId(id));
    }

    @Transactional(readOnly = true)
    public List<AccreditationResponse> listerParEvenement(UUID evenementId) {
        return accreditationRepository.findByEvenementId(evenementId)
            .stream()
            .map(accreditationMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AccreditationResponse> listerParBenevole(UUID benevoleId) {
        return accreditationRepository.findByBenevoleId(benevoleId)
            .stream()
            .map(accreditationMapper::toResponse)
            .toList();
    }

    /**
     * Retourne l'image QR code en bytes PNG pour l'accréditation demandée.
     * Utilisé par l'endpoint dédié image (/qr).
     */
    @Transactional(readOnly = true)
    public byte[] obtenirImageQr(UUID id) {
        Accreditation accreditation = trouverParId(id);
        return qrCodeService.genererQrCodeBytes(accreditation.getCodeQr());
    }

    @Transactional
    public void supprimer(UUID id) {
        accreditationRepository.delete(trouverParId(id));
    }

    // --- Méthodes privées ---

    private Accreditation trouverParId(UUID id) {
        return accreditationRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Accréditation introuvable"));
    }
}
