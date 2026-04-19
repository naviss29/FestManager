package com.festmanager.service;

import com.festmanager.config.JwtUtils;
import com.festmanager.dto.LoginRequest;
import com.festmanager.dto.LoginResponse;
import com.festmanager.dto.RegisterRequest;
import com.festmanager.dto.RegisterResponse;
import com.festmanager.entity.Utilisateur;
import com.festmanager.entity.enums.RoleUtilisateur;
import com.festmanager.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
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
     * Lève 403 si le compte est inactif (en attente de validation admin).
     */
    public LoginResponse connecter(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.motDePasse())
            );
        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Compte en attente de validation par un administrateur.");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.email())
                .orElseThrow();

        String token = jwtUtils.genererToken(utilisateur);
        return new LoginResponse(token, utilisateur.getEmail(), utilisateur.getRole().name());
    }

    /**
     * Création de compte.
     * - Premier compte créé → ADMIN, actif immédiatement (bootstrap).
     * - Comptes suivants → ORGANISATEUR, actif = false (en attente de validation admin).
     */
    @Transactional
    public RegisterResponse inscrire(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un compte existe déjà avec cet email.");
        }

        boolean estPremierCompte = utilisateurRepository.count() == 0;
        RoleUtilisateur role = estPremierCompte ? RoleUtilisateur.ADMIN : RoleUtilisateur.ORGANISATEUR;

        Utilisateur utilisateur = Utilisateur.builder()
                .email(request.email())
                .motDePasse(passwordEncoder.encode(request.motDePasse()))
                .role(role)
                // Premier compte (admin) actif immédiatement ; les suivants attendent validation
                .actif(estPremierCompte)
                .build();

        utilisateurRepository.save(utilisateur);

        if (estPremierCompte) {
            String token = jwtUtils.genererToken(utilisateur);
            return new RegisterResponse(utilisateur.getEmail(), role.name(), token, false);
        }
        return new RegisterResponse(utilisateur.getEmail(), role.name(), null, true);
    }
}
