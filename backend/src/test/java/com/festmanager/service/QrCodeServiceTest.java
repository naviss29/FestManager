package com.festmanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("QrCodeService — génération QR")
class QrCodeServiceTest {

    private QrCodeService service;

    @BeforeEach
    void setUp() {
        service = new QrCodeService();
    }

    @Test
    @DisplayName("genererQrCodeBytes produit un PNG non vide")
    void genererQrCodeBytes_produitPngNonVide() {
        byte[] bytes = service.genererQrCodeBytes("FESTMANAGER:test-uuid");

        assertThat(bytes).isNotEmpty();
        // Signature PNG : 8 premiers octets = 89 50 4E 47 0D 0A 1A 0A
        assertThat(bytes[0]).isEqualTo((byte) 0x89);
        assertThat(bytes[1]).isEqualTo((byte) 0x50); // 'P'
        assertThat(bytes[2]).isEqualTo((byte) 0x4E); // 'N'
        assertThat(bytes[3]).isEqualTo((byte) 0x47); // 'G'
    }

    @Test
    @DisplayName("genererQrCodeBase64 retourne une chaîne base64 valide")
    void genererQrCodeBase64_retourneBase64Valide() {
        String base64 = service.genererQrCodeBase64("FESTMANAGER:test-uuid");

        assertThat(base64).isNotBlank();
        // Doit être décodable sans exception
        byte[] decoded = Base64.getDecoder().decode(base64);
        assertThat(decoded).isNotEmpty();
    }

    @Test
    @DisplayName("genererQrCodeBytes fonctionne avec un contenu long")
    void genererQrCodeBytes_contenuLong() {
        String contenuLong = "FESTMANAGER:" + "a".repeat(200);
        byte[] bytes = service.genererQrCodeBytes(contenuLong);

        assertThat(bytes).isNotEmpty();
    }

    @Test
    @DisplayName("genererQrCodeBytes lève une exception si le contenu est vide")
    void genererQrCodeBytes_contenuVide_leveException() {
        assertThatThrownBy(() -> service.genererQrCodeBytes(""))
            .isInstanceOf(RuntimeException.class);
    }
}
