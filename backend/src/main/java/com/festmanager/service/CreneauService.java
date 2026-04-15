package com.festmanager.service;

import com.festmanager.dto.CreneauRequest;
import com.festmanager.dto.CreneauResponse;
import com.festmanager.entity.Creneau;
import com.festmanager.entity.Mission;
import com.festmanager.mapper.CreneauMapper;
import com.festmanager.repository.CreneauRepository;
import com.festmanager.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreneauService {

    private final CreneauRepository creneauRepository;
    private final MissionRepository missionRepository;
    private final CreneauMapper creneauMapper;

    @Transactional(readOnly = true)
    public List<CreneauResponse> listerCreneaux(UUID missionId) {
        verifierMissionExiste(missionId);
        return creneauRepository.findByMissionId(missionId).stream()
                .map(creneauMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CreneauResponse obtenirCreneau(UUID id) {
        return creneauMapper.toResponse(trouverParId(id));
    }

    @Transactional
    public CreneauResponse creerCreneau(UUID missionId, CreneauRequest request) {
        validerDates(request);
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mission introuvable"));
        Creneau creneau = creneauMapper.toEntity(request, mission);
        return creneauMapper.toResponse(creneauRepository.save(creneau));
    }

    @Transactional
    public CreneauResponse modifierCreneau(UUID id, CreneauRequest request) {
        validerDates(request);
        Creneau creneau = trouverParId(id);
        creneau.setDebut(request.getDebut());
        creneau.setFin(request.getFin());
        creneau.setNbBenevolesRequis(request.getNbBenevolesRequis());
        return creneauMapper.toResponse(creneauRepository.save(creneau));
    }

    @Transactional
    public void supprimerCreneau(UUID id) {
        creneauRepository.delete(trouverParId(id));
    }

    private Creneau trouverParId(UUID id) {
        return creneauRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Créneau introuvable"));
    }

    private void verifierMissionExiste(UUID missionId) {
        if (!missionRepository.existsById(missionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mission introuvable");
        }
    }

    private void validerDates(CreneauRequest request) {
        if (!request.getFin().isAfter(request.getDebut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fin du créneau doit être après son début");
        }
    }
}
