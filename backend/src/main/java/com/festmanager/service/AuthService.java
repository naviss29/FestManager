package com.festmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.festmanager.config.JwtUtils;
import com.festmanager.dto.GoogleAuthRequest;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.google.client-id:}")
    private String googleClientId;

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
     * Connexion ou inscription via Google Identity Services.
     * - Compte déjà lié → connexion directe.
     * - Email existant → liaison du Google ID puis connexion.
     * - Nouveau compte → création (admin si premier, sinon en attente de validation).
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public RegisterResponse connexionGoogle(GoogleAuthRequest request) {
        Map<String, Object> googlePayload = validerTokenGoogle(request.credential());

        String email    = (String) googlePayload.get("email");
        String googleId = (String) googlePayload.get("sub");

        // Cas 1 : compte déjà lié à cet identifiant Google
        Optional<Utilisateur> parGoogleId = utilisateurRepository.findByGoogleId(googleId);
        if (parGoogleId.isPresent()) {
            return connecterUtilisateur(parGoogleId.get());
        }

        // Cas 2 : compte existant par email → on lie le Google ID
        Optional<Utilisateur> parEmail = utilisateurRepository.findByEmail(email);
        if (parEmail.isPresent()) {
            Utilisateur u = parEmail.get();
            u.setGoogleId(googleId);
            utilisateurRepository.save(u);
            return connecterUtilisateur(u);
        }

        // Cas 3 : nouvel utilisateur
        boolean estPremierCompte = utilisateurRepository.count() == 0;
        RoleUtilisateur role = estPremierCompte ? RoleUtilisateur.ADMIN : RoleUtilisateur.ORGANISATEUR;

        Utilisateur nouveau = Utilisateur.builder()
                .email(email)
                .motDePasse(passwordEncoder.encode(UUID.randomUUID().toString()))
                .googleId(googleId)
                .role(role)
                .actif(estPremierCompte)
                .build();
        utilisateurRepository.save(nouveau);

        if (estPremierCompte) {
            return new RegisterResponse(nouveau.getEmail(), role.name(), jwtUtils.genererToken(nouveau), false);
        }
        return new RegisterResponse(nouveau.getEmail(), role.name(), null, true);
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

    // --- Méthodes privées ---

    private RegisterResponse connecterUtilisateur(Utilisateur u) {
        if (!u.getActif()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Compte en attente de validation par un administrateur.");
        }
        return new RegisterResponse(u.getEmail(), u.getRole().name(), jwtUtils.genererToken(u), false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Object> validerTokenGoogle(String idToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map payload = restTemplate.getForObject(
                    "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken, Map.class);

            if (payload == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Google invalide.");
            }

            // Vérification de l'audience : doit correspondre au client ID configuré
            if (!googleClientId.isBlank()) {
                String aud = (String) payload.get("aud");
                if (!googleClientId.equals(aud)) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Token Google invalide : audience incorrecte.");
                }
            }

            return (Map<String, Object>) payload;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Impossible de valider le token Google.");
        }
    }
}
