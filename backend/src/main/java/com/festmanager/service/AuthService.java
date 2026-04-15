package com.festmanager.service;

import com.festmanager.config.JwtUtils;
import com.festmanager.dto.LoginRequest;
import com.festmanager.dto.LoginResponse;
import com.festmanager.dto.RegisterRequest;
import com.festmanager.entity.Utilisateur;
import com.festmanager.entity.enums.RoleUtilisateur;
import com.festmanager.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Connexion — retourne un JWT signé avec le rôle en claim.
     */
    public LoginResponse connecter(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.motDePasse())
        );

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.email())
                .orElseThrow();

        String token = jwtUtils.genererToken(utilisateur);
        return new LoginResponse(token, utilisateur.getEmail(), utilisateur.getRole().name());
    }

    /**
     * Création de compte.
     * - Premier compte créé → ADMIN (bootstrap).
     * - Comptes suivants → ORGANISATEUR.
     */
    @Transactional
    public LoginResponse inscrire(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un compte existe déjà avec cet email.");
        }

        RoleUtilisateur role = utilisateurRepository.count() == 0
                ? RoleUtilisateur.ADMIN
                : RoleUtilisateur.ORGANISATEUR;

        Utilisateur utilisateur = Utilisateur.builder()
                .email(request.email())
                .motDePasse(passwordEncoder.encode(request.motDePasse()))
                .role(role)
                .actif(true)
                .build();

        utilisateurRepository.save(utilisateur);

        String token = jwtUtils.genererToken(utilisateur);
        return new LoginResponse(token, utilisateur.getEmail(), utilisateur.getRole().name());
    }
}
