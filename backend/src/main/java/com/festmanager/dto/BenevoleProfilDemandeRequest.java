package com.festmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record BenevoleProfilDemandeRequest(
    @NotBlank @Email String email
) {}
