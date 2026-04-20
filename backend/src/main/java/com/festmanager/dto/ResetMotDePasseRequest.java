package com.festmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetMotDePasseRequest(
    @NotBlank String token,
    @NotBlank @Size(min = 8) String nouveauMotDePasse
) {}
