package com.festmanager.service;

import com.festmanager.entity.Accreditation;
import com.festmanager.entity.Benevole;
import com.festmanager.entity.Evenement;
import com.festmanager.entity.enums.TypeAccreditation;
import com.festmanager.entity.enums.ZoneAcces;
import com.festmanager.repository.AccreditationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BadgeService")
class BadgeServiceTest {

    @Mock AccreditationRepository accreditationRepository;
    @Mock QrCodeService qrCodeService;
    @Mock FichierService fichierService;

    @InjectMocks BadgeService service;

    // PNG 50×50 généré programmatiquement — valide pour OpenPDF Image.getInstance()
    private static final byte[] MINI_PNG = creerPngTest();

    private static byte[] creerPngTest() {
        try {
            BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            ImageIO.write(img, "PNG", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le PNG de test", e);
        }
    }

    private UUID accreditationId;
    private UUID evenementId;
    private Accreditation accreditation;

    @BeforeEach
    void setUp() {
        accreditationId = UUID.randomUUID();
        evenementId     = UUID.randomUUID();

        Benevole benevole = Benevole.builder()
                .id(UUID.randomUUID())
                .nom("Dupont")
                .prenom("Alice")
                .build();

        Evenement evenement = new Evenement();
        evenement.setId(evenementId);
        evenement.setNom("FestTest 2026");

        accreditation = Accreditation.builder()
                .id(accreditationId)
                .benevole(benevole)
                .evenement(evenement)
                .type(TypeAccreditation.BENEVOLE)
                .zonesAcces(Set.of(ZoneAcces.GENERAL, ZoneAcces.SCENE))
                .codeQr("FESTMANAGER:" + accreditationId)
                .valide(true)
                .dateEmission(LocalDateTime.of(2026, 4, 1, 9, 0))
                .dateDebutValidite(LocalDate.of(2026, 7, 1))
                .dateFinValidite(LocalDate.of(2026, 7, 3))
                .build();
    }

    // -------------------------------------------------------------------------
    // genererBadge
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("genererBadge — retourne un PDF non vide commençant par %PDF")
    void genererBadge_retourneUnPdfValide() {
        when(accreditationRepository.findById(accreditationId)).thenReturn(Optional.of(accreditation));
        when(qrCodeService.genererQrCodeBytes(accreditation.getCodeQr())).thenReturn(MINI_PNG);

        byte[] resultat = service.genererBadge(accreditationId);

        assertThat(resultat).isNotEmpty();
        // Vérification que c'est bien un PDF (magic bytes %PDF)
        assertThat(new String(resultat, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("genererBadge — lève NOT_FOUND si l'accréditation est introuvable")
    void genererBadge_leveNotFoundSiManquante() {
        when(accreditationRepository.findById(accreditationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.genererBadge(accreditationId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    @DisplayName("genererBadge — fonctionne sans dates de validité (affiche durée événement)")
    void genererBadge_sansDateValidite() {
        accreditation = Accreditation.builder()
                .id(accreditationId)
                .benevole(accreditation.getBenevole())
                .evenement(accreditation.getEvenement())
                .type(TypeAccreditation.STAFF)
                .zonesAcces(Set.of(ZoneAcces.BACKSTAGE))
                .codeQr("FESTMANAGER:" + accreditationId)
                .valide(true)
                .dateEmission(LocalDateTime.now())
                // Pas de dates de validité
                .build();

        when(accreditationRepository.findById(accreditationId)).thenReturn(Optional.of(accreditation));
        when(qrCodeService.genererQrCodeBytes(accreditation.getCodeQr())).thenReturn(MINI_PNG);

        byte[] resultat = service.genererBadge(accreditationId);

        assertThat(resultat).isNotEmpty();
        assertThat(new String(resultat, 0, 4)).isEqualTo("%PDF");
    }

    // -------------------------------------------------------------------------
    // genererBadgesZip
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("genererBadgesZip — retourne un ZIP non vide pour un événement avec accréditations")
    void genererBadgesZip_retourneUnZipValide() {
        when(accreditationRepository.findByEvenementId(evenementId))
                .thenReturn(List.of(accreditation));
        when(qrCodeService.genererQrCodeBytes(accreditation.getCodeQr())).thenReturn(MINI_PNG);

        byte[] resultat = service.genererBadgesZip(evenementId);

        assertThat(resultat).isNotEmpty();
        // Magic bytes ZIP : PK (0x50 0x4B)
        assertThat(resultat[0]).isEqualTo((byte) 0x50);
        assertThat(resultat[1]).isEqualTo((byte) 0x4B);
    }

    @Test
    @DisplayName("genererBadgesZip — retourne un ZIP vide si aucune accréditation")
    void genererBadgesZip_zipVideSiAucuneAccreditation() {
        when(accreditationRepository.findByEvenementId(evenementId)).thenReturn(List.of());

        byte[] resultat = service.genererBadgesZip(evenementId);

        // Un ZIP vide est tout de même un fichier ZIP valide (magic bytes PK)
        assertThat(resultat).isNotEmpty();
        assertThat(resultat[0]).isEqualTo((byte) 0x50);
        assertThat(resultat[1]).isEqualTo((byte) 0x4B);
    }
}
