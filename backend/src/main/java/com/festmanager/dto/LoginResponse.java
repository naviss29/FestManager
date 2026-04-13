package com.festmanager.dto;

public record LoginResponse(
        String token,
        String email,
        String role
) {}
