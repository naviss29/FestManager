package com.festmanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FichierService")
class FichierServiceTest {

    @TempDir
    Path tempDir;

    FichierService service;

    @BeforeEach
    void setUp() {
        service = new FichierService();
        // Injection du répertoire temporaire via ReflectionTestUtils (champ @Value)
        ReflectionTestUtils.setField(service, "uploadsDir", tempDir.toString());
    }

    // ── sauvegarder ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("sauvegarder — crée le fichier sur le disque et retourne l'URL relative")
    void sauvegarder_creeLeEtRetourneUrl() throws Exception {
        MockMultipartFile fichier = new MockMultipartFile(
                "fichier", "photo.jpg", "image/jpeg", "contenu-test".getBytes());

        String url = service.sauvegarder(fichier, "benevoles", "abc-123");

        assertThat(url).isEqualTo("/api/fichiers/benevoles/abc-123.jpg");
        Path cible = tempDir.resolve("benevoles/abc-123.jpg");
        assertThat(cible).exists();
        assertThat(Files.readString(cible)).isEqualTo("contenu-test");
    }

    @Test
    @DisplayName("sauvegarder — remplace un fichier existant du même nomBase")
    void sauvegarder_remplaceFichierExistant() throws Exception {
        MockMultipartFile v1 = new MockMultipartFile("f", "p.png", "image/png", "v1".getBytes());
        MockMultipartFile v2 = new MockMultipartFile("f", "p.png", "image/png", "v2".getBytes());

        service.sauvegarder(v1, "benevoles", "abc-123");
        service.sauvegarder(v2, "benevoles", "abc-123");

        Path cible = tempDir.resolve("benevoles/abc-123.png");
        assertThat(Files.readString(cible)).isEqualTo("v2");
    }

    @Test
    @DisplayName("sauvegarder — lève BAD_REQUEST si le fichier est vide")
    void sauvegarder_leveErreurFichierVide() {
        MockMultipartFile vide = new MockMultipartFile("f", "p.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> service.sauvegarder(vide, "benevoles", "id"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("vide");
    }

    @Test
    @DisplayName("sauvegarder — lève BAD_REQUEST si le content-type n'est pas une image")
    void sauvegarder_leveErreurTypeInvalide() {
        MockMultipartFile pdf = new MockMultipartFile("f", "doc.pdf", "application/pdf", "data".getBytes());

        assertThatThrownBy(() -> service.sauvegarder(pdf, "benevoles", "id"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("non accepté");
    }

    // ── charger ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("charger — retourne le contenu du fichier existant")
    void charger_retourneLeContenu() throws Exception {
        Path rep = tempDir.resolve("benevoles");
        Files.createDirectories(rep);
        Files.writeString(rep.resolve("abc.jpg"), "image-data");

        byte[] resultat = service.charger("benevoles", "abc.jpg");

        assertThat(new String(resultat)).isEqualTo("image-data");
    }

    @Test
    @DisplayName("charger — lève NOT_FOUND si le fichier est absent")
    void charger_leveNotFoundSiAbsent() {
        assertThatThrownBy(() -> service.charger("benevoles", "inexistant.jpg"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    @DisplayName("charger — lève BAD_REQUEST en cas de path traversal hors du répertoire uploads")
    void charger_leveErreurPathTraversal() {
        // ../../ remonte au-dessus du répertoire uploads — doit être rejeté
        assertThatThrownBy(() -> service.charger("benevoles", "../../etc/passwd"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("invalide");
    }

    // ── supprimer ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("supprimer — efface le fichier du disque")
    void supprimer_effaceLeFichier() throws Exception {
        Path rep = tempDir.resolve("benevoles");
        Files.createDirectories(rep);
        Path cible = rep.resolve("abc-123.jpg");
        Files.writeString(cible, "data");

        service.supprimer("benevoles", "abc-123");

        assertThat(cible).doesNotExist();
    }

    @Test
    @DisplayName("supprimer — ne lève pas d'erreur si le fichier n'existe pas")
    void supprimer_silencieuxSiAbsent() {
        assertThatCode(() -> service.supprimer("benevoles", "inconnu"))
                .doesNotThrowAnyException();
    }
}
