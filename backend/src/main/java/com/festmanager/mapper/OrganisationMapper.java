package com.festmanager.mapper;

import com.festmanager.dto.OrganisationRequest;
import com.festmanager.dto.OrganisationResponse;
import com.festmanager.entity.Organisation;
import org.springframework.stereotype.Component;

@Component
public class OrganisationMapper {

    public Organisation toEntity(OrganisationRequest request) {
        return Organisation.builder()
                .nom(request.getNom())
                .type(request.getType())
                .siret(request.getSiret())
                .emailContact(request.getEmailContact())
                .telephoneContact(request.getTelephoneContact())
                .adresse(request.getAdresse())
                .build();
    }

    public OrganisationResponse toResponse(Organisation organisation) {
        OrganisationResponse response = new OrganisationResponse();
        response.setId(organisation.getId());
        response.setNom(organisation.getNom());
        response.setType(organisation.getType());
        response.setSiret(organisation.getSiret());
        response.setEmailContact(organisation.getEmailContact());
        response.setTelephoneContact(organisation.getTelephoneContact());
        response.setAdresse(organisation.getAdresse());
        response.setCreatedAt(organisation.getCreatedAt());
        return response;
    }
}
