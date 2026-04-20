package com.festmanager.service;

import com.festmanager.config.JwtUtils;
import com.festmanager.dto.LoginRequest;
import com.festmanager.dto.LoginResponse;
import com.festmanager.dto.MotDePasseOublieRequest;
import com.festmanager.dto.RegisterRequest;
import com.festmanager.dto.RegisterResponse;
import com.festmanager.dto.ResetMotDePasseRequest;
import com.festmanager.entity.Utilisateur;
import com.festmanager.entity.enums.RoleUtilisateur;
import com.festmanager.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

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

    /**
     * Génère un token de reset valable 1h et envoie l'email.
     * Répond toujours 200 même si l'email est inconnu (sécurité anti-énumération).
     */
    @Transactional
    public void demanderResetMotDePasse(MotDePasseOublieRequest request) {
        utilisateurRepository.findByEmail(request.email()).ifPresent(utilisateur -> {
            String token = UUID.randomUUID().toString();
            utilisateur.setResetToken(token);
            utilisateur.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            utilisateurRepository.save(utilisateur);

            String lien = frontendUrl + "/auth/reset-mot-de-passe?token=" + token;
            emailService.envoyerResetMotDePasse(utilisateur.getEmail(), lien);
        });
    }

    /**
     * Valide le token et met à jour le mot de passe.
     * Lève 400 si le token est invalide ou expiré.
     */
    @Transactional
    public void resetMotDePasse(ResetMotDePasseRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByResetToken(request.token())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Lien de réinitialisation invalide ou expiré."));

        if (utilisateur.getResetTokenExpiry() == null ||
                utilisateur.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Lien de réinitialisation invalide ou expiré.");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(request.nouveauMotDePasse()));
        utilisateur.setResetToken(null);
        utilisateur.setResetTokenExpiry(null);
        utilisateurRepository.save(utilisateur);
    }
}
