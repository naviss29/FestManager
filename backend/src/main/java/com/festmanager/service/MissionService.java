package com.festmanager.service;

import com.festmanager.dto.MissionRequest;
import com.festmanager.dto.MissionResponse;
import com.festmanager.entity.Evenement;
import com.festmanager.entity.Mission;
import com.festmanager.entity.Organisation;
import com.festmanager.entity.enums.CategorieMission;
import com.festmanager.mapper.MissionMapper;
import com.festmanager.repository.EvenementRepository;
import com.festmanager.repository.MissionRepository;
import com.festmanager.repository.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final EvenementRepository evenementRepository;
    private final OrganisationRepository organisationRepository;
    private final MissionMapper missionMapper;

    public Page<MissionResponse> listerMissions(UUID evenementId, CategorieMission categorie, Pageable pageable) {
        verifierEvenementExiste(evenementId);
        Page<Mission> missions = (categorie != null)
                ? missionRepository.findByEvenementIdAndCategorie(evenementId, categorie, pageable)
                : missionRepository.findByEvenementId(evenementId, pageable);
        return missions.map(missionMapper::toResponse);
    }

    public MissionResponse obtenirMission(UUID id) {
        return missionMapper.toResponse(trouverParId(id));
    }

    @Transactional
    public MissionResponse creerMission(UUID evenementId, MissionRequest request) {
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable"));

        Organisation organisation = null;
        if (Boolean.TRUE.equals(request.getGereeParOrganisation())) {
            if (request.getOrganisationId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "L'organisationId est obligatoire si la mission est gérée par une organisation");
            }
            organisation = organisationRepository.findById(request.getOrganisationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organisation introuvable"));
        }

        Mission mission = missionMapper.toEntity(request, evenement, organisation);
        return missionMapper.toResponse(missionRepository.save(mission));
    }

    @Transactional
    public MissionResponse modifierMission(UUID id, MissionRequest request) {
        Mission mission = trouverParId(id);

        Organisation organisation = null;
        if (Boolean.TRUE.equals(request.getGereeParOrganisation())) {
            if (request.getOrganisationId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "L'organisationId est obligatoire si la mission est gérée par une organisation");
            }
            organisation = organisationRepository.findById(request.getOrganisationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organisation introuvable"));
        }

        mission.setNom(request.getNom());
        mission.setDescription(request.getDescription());
        mission.setLieu(request.getLieu());
        mission.setMaterielRequis(request.getMaterielRequis());
        mission.setCategorie(request.getCategorie());
        mission.setNbBenevolesRequis(request.getNbBenevolesRequis());
        mission.setMultiAffectationAutorisee(Boolean.TRUE.equals(request.getMultiAffectationAutorisee()));
        mission.setGereeParOrganisation(Boolean.TRUE.equals(request.getGereeParOrganisation()));
        mission.setOrganisation(organisation);

        return missionMapper.toResponse(missionRepository.save(mission));
    }

    @Transactional
    public void supprimerMission(UUID id) {
        missionRepository.delete(trouverParId(id));
    }

    private Mission trouverParId(UUID id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mission introuvable"));
    }

    private void verifierEvenementExiste(UUID evenementId) {
        if (!evenementRepository.existsById(evenementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable");
        }
    }
}
