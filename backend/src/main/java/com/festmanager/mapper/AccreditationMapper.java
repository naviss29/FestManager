package com.festmanager.mapper;

import com.festmanager.dto.AccreditationResponse;
import com.festmanager.entity.Accreditation;
import com.festmanager.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccreditationMapper {

    private final QrCodeService qrCodeService;

    public AccreditationResponse toResponse(Accreditation accreditation) {
        AccreditationResponse response = new AccreditationResponse();
        response.setId(accreditation.getId());
        response.setBenevoleId(accreditation.getBenevole().getId());
        response.setBenevoleNom(accreditation.getBenevole().getNom());
        response.setBenevolePrenom(accreditation.getBenevole().getPrenom());
        response.setEvenementId(accreditation.getEvenement().getId());
        response.setEvenementNom(accreditation.getEvenement().getNom());
        response.setType(accreditation.getType());
        response.setZonesAcces(accreditation.getZonesAcces());
        response.setDateDebutValidite(accreditation.getDateDebutValidite());
        response.setDateFinValidite(accreditation.getDateFinValidite());
        response.setCodeQr(accreditation.getCodeQr());
        response.setQrBase64(qrCodeService.genererQrCodeBase64(accreditation.getCodeQr()));
        response.setValide(accreditation.getValide());
        response.setDateEmission(accreditation.getDateEmission());
        return response;
    }
}
