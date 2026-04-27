package com.festmanager.controller;

import com.festmanager.dto.GoogleAuthRequest;
import com.festmanager.dto.LoginRequest;
import com.festmanager.dto.LoginResponse;
import com.festmanager.dto.MotDePasseOublieRequest;
import com.festmanager.dto.RegisterRequest;
import com.festmanager.dto.RegisterResponse;
import com.festmanager.dto.ResetMotDePasseRequest;
import com.festmanager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Login et inscription — endpoints publics, aucun token requis")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Connexion", description = "Retourne un token JWT à inclure dans les requêtes suivantes.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.connecter(request));
    }

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(
        summary = "Inscription",
        description = "Premier compte → ADMIN actif immédiatement (token fourni). " +
                      "Comptes suivants → ORGANISATEUR en attente de validation admin (pas de token).")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.inscrire(request);
        int status = response.enAttenteValidation() ? 202 : 201;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/mot-de-passe-oublie")
    @SecurityRequirements
    @Operation(summary = "Mot de passe oublié",
               description = "Envoie un email de réinitialisation. Répond toujours 200 même si l'email est inconnu.")
    public ResponseEntity<Void> motDePasseOublie(@Valid @RequestBody MotDePasseOublieRequest request) {
        authService.demanderResetMotDePasse(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-mot-de-passe")
    @SecurityRequirements
    @Operation(summary = "Réinitialiser le mot de passe",
               description = "Valide le token et met à jour le mot de passe. Token valable 1h.")
    public ResponseEntity<Void> resetMotDePasse(@Valid @RequestBody ResetMotDePasseRequest request) {
        authService.resetMotDePasse(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/google")
    @SecurityRequirements
    @Operation(
        summary = "Connexion via Google",
        description = "Valide un ID token Google Identity Services. " +
                      "Lie le compte si l'email existe, en crée un sinon (en attente de validation si ce n'est pas le premier compte).")
    public ResponseEntity<RegisterResponse> google(@Valid @RequestBody GoogleAuthRequest request) {
        RegisterResponse response = authService.connexionGoogle(request);
        int status = response.enAttenteValidation() ? 202 : 200;
        return ResponseEntity.status(status).body(response);
    }
}
