package com.festmanager.dto;

import com.festmanager.entity.enums.TailleTshirt;

public record BenevoleProfilResponse(
    String id,
    String nom,
    String prenom,
    String email,
    String telephone,
    TailleTshirt tailleTshirt,
    String competences,
    String disponibilites,
    String photoUrl
) {}
