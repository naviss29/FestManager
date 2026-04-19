package com.festmanager.dto;

/**
 * Réponse à l'inscription.
 * Si enAttenteValidation = true, le compte est inactif et attend validation admin.
 * Si enAttenteValidation = false, le compte est immédiatement actif (premier admin) et token est fourni.
 */
public record RegisterResponse(
        String email,
        String role,
        String token,
        boolean enAttenteValidation
) {}
