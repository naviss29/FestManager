package com.festmanager.service;

import com.festmanager.dto.UtilisateurResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UtilisateurService")
class UtilisateurServiceTest {

    @Mock UtilisateurRepository utilisateurRepository;

    @InjectMocks UtilisateurService service;

    private UUID id;
    private Utilisateur utilisateurInactif;
    private Utilisateur utilisateurActif;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();

        utilisateurInactif = Utilisateur.builder()
                .id(id)
                .email("attente@fest.fr")
                .role(RoleUtilisateur.ORGANISATEUR)
                .actif(false)
                .build();

        utilisateurActif = Utilisateur.builder()
                .id(id)
                .email("actif@fest.fr")
                .role(RoleUtilisateur.ORGANISATEUR)
                .actif(true)
                .build();
    }

    // ── lister ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("lister — enAttente=true retourne uniquement les comptes inactifs")
    void lister_enAttenteRetourneInactifs() {
        Page<Utilisateur> page = new PageImpl<>(List.of(utilisateurInactif));
        when(utilisateurRepository.findByActif(false, Pageable.unpaged())).thenReturn(page);

        Page<UtilisateurResponse> result = service.lister(true, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isActif()).isFalse();
    }

    @Test
    @DisplayName("lister — enAttente=false retourne tous les comptes")
    void lister_sansFiltreTousLesComptes() {
        Page<Utilisateur> page = new PageImpl<>(List.of(utilisateurInactif, utilisateurActif));
        when(utilisateurRepository.findAll(Pageable.unpaged())).thenReturn(page);

        Page<UtilisateurResponse> result = service.lister(false, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(2);
    }

    // ── valider ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("valider — active le compte inactif")
    void valider_activeLeCompte() {
        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(utilisateurInactif));
        when(utilisateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UtilisateurResponse result = service.valider(id);

        assertThat(result.isActif()).isTrue();
        verify(utilisateurRepository).save(utilisateurInactif);
    }

    @Test
    @DisplayName("valider — lève CONFLICT si le compte est déjà actif")
    void valider_leveConflitSiDejaActif() {
        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(utilisateurActif));

        assertThatThrownBy(() -> service.valider(id))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("déjà actif");
    }

    @Test
    @DisplayName("valider — lève NOT_FOUND si l'utilisateur est introuvable")
    void valider_leveNotFound() {
        when(utilisateurRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.valider(id))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("introuvable");
    }

    // ── rejeter ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("rejeter — supprime le compte inactif")
    void rejeter_supprimeLeDemandeur() {
        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(utilisateurInactif));

        service.rejeter(id);

        verify(utilisateurRepository).delete(utilisateurInactif);
    }

    @Test
    @DisplayName("rejeter — lève CONFLICT si le compte est déjà actif")
    void rejeter_leveConflitSiActif() {
        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(utilisateurActif));

        assertThatThrownBy(() -> service.rejeter(id))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Impossible");
    }
}
