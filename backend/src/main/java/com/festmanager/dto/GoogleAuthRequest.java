package com.festmanager.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
    @NotBlank String credential
) {}
