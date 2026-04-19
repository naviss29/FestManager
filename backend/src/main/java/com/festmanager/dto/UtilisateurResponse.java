package com.festmanager.dto;

import com.festmanager.entity.enums.RoleUtilisateur;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Vue d'un compte utilisateur destinée à l'interface d'administration.
 */
@Data
public class UtilisateurResponse {
    private UUID id;
    private String email;
    private RoleUtilisateur role;
    private boolean actif;
    private LocalDateTime createdAt;
    private LocalDateTime derniereConnexion;
}
