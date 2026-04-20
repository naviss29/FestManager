package com.festmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MotDePasseOublieRequest(
    @NotBlank @Email String email
) {}
