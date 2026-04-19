package com.festmanager.service;

import com.festmanager.config.JwtUtils;
import com.festmanager.dto.LoginRequest;
import com.festmanager.dto.LoginResponse;
import com.festmanager.dto.RegisterRequest;
import com.festmanager.dto.RegisterResponse;
import com.festmanager.entity.Utilisateur;
import com.festmanager.entity.enums.RoleUtilisateur;
import com.festmanager.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock UtilisateurRepository utilisateurRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtUtils jwtUtils;

    @InjectMocks AuthService service;

    private static final String EMAIL = "alan@festmanager.fr";
    private static final String MOT_DE_PASSE = "secret123";

    // -------------------------------------------------------------------------
    // connecter
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("connecter — retourne un JWT avec l'email et le rôle")
    void connecter_retourneJwt() {
        Utilisateur utilisateur = Utilisateur.builder()
                .email(EMAIL)
                .role(RoleUtilisateur.ORGANISATEUR)
                .build();

        when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(utilisateur));
        when(jwtUtils.genererToken(utilisateur)).thenReturn("jwt-token");

        LoginResponse result = service.connecter(new LoginRequest(EMAIL, MOT_DE_PASSE));

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.email()).isEqualTo(EMAIL);
        assertThat(result.role()).isEqualTo("ORGANISATEUR");
        verify(authenticationManager).authenticate(any());
    }

    // -------------------------------------------------------------------------
    // inscrire
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("inscrire — premier compte reçoit ADMIN, est actif immédiatement et reçoit un token")
    void inscrire_premierCompteEstAdmin() {
        when(utilisateurRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(utilisateurRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(MOT_DE_PASSE)).thenReturn("hash");
        when(utilisateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtils.genererToken(any())).thenReturn("jwt-token");

        RegisterResponse result = service.inscrire(new RegisterRequest(EMAIL, MOT_DE_PASSE));

        assertThat(result.role()).isEqualTo("ADMIN");
        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.enAttenteValidation()).isFalse();
        verify(utilisateurRepository).save(argThat(u ->
                u.getRole() == RoleUtilisateur.ADMIN && u.getActif()));
    }

    @Test
    @DisplayName("inscrire — compte suivant est ORGANISATEUR, inactif, sans token (en attente de validation)")
    void inscrire_comptesSuivantsEnAttenteValidation() {
        when(utilisateurRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(utilisateurRepository.count()).thenReturn(5L);
        when(passwordEncoder.encode(MOT_DE_PASSE)).thenReturn("hash");
        when(utilisateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterResponse result = service.inscrire(new RegisterRequest(EMAIL, MOT_DE_PASSE));

        assertThat(result.role()).isEqualTo("ORGANISATEUR");
        assertThat(result.token()).isNull();
        assertThat(result.enAttenteValidation()).isTrue();
        verify(utilisateurRepository).save(argThat(u ->
                u.getRole() == RoleUtilisateur.ORGANISATEUR && !u.getActif()));
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("inscrire — lève CONFLICT si l'email est déjà utilisé")
    void inscrire_leveConflitSiEmailPris() {
        when(utilisateurRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> service.inscrire(new RegisterRequest(EMAIL, MOT_DE_PASSE)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("déjà");
    }
}
