package com.festmanager.dto;

import com.festmanager.entity.enums.TailleTshirt;
import jakarta.validation.constraints.Size;

public record BenevoleProfilUpdateRequest(
    @Size(max = 20) String telephone,
    TailleTshirt tailleTshirt,
    String competences,
    String disponibilites
) {}
