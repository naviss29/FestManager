package com.festmanager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.stream.Stream;

/**
 * Gère la sauvegarde, la lecture et la suppression de fichiers sur le système de fichiers local.
 *
 * Répertoire racine : app.uploads.dir (défaut ./uploads).
 * Structure : {uploadsDir}/{sousRep}/{nomBase}.{ext}
 *
 * En production Docker, monter un volume persistant sur ce répertoire.
 */
@Service
public class FichierService {

    @Value("${app.uploads.dir}")
    private String uploadsDir;

    /**
     * Sauvegarde un fichier uploadé dans {uploadsDir}/{sousRep}/{nomBase}.{ext}.
     * Supprime toute version précédente du même nomBase avant d'écrire.
     *
     * @param fichier     fichier reçu en multipart
     * @param sousRep     sous-répertoire (ex : "benevoles", "evenements")
     * @param nomBase     nom sans extension (ex : UUID du bénévole)
     * @return URL relative prête à stocker en base : /api/fichiers/{sousRep}/{nomBase}.{ext}
     */
    public String sauvegarder(MultipartFile fichier, String sousRep, String nomBase) {
        validerFichier(fichier);

        String extension = obtenirExtension(fichier.getOriginalFilename());
        Path repertoire = resoudreRepertoire(sousRep);

        try {
            Files.createDirectories(repertoire);
            supprimerExistants(repertoire, nomBase);

            String nomFichier = nomBase + extension;
            Path cible = repertoire.resolve(nomFichier).normalize();
            verifierConfinement(cible, repertoire);

            try (InputStream flux = fichier.getInputStream()) {
                Files.copy(flux, cible, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/api/fichiers/" + sousRep + "/" + nomFichier;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier", e);
        }
    }

    /**
     * Charge le contenu d'un fichier en mémoire.
     *
     * @throws ResponseStatusException 404 si le fichier n'existe pas
     */
    public byte[] charger(String sousRep, String nomFichier) {
        Path chemin = resoudreRepertoire(sousRep).resolve(nomFichier).normalize();
        verifierConfinement(chemin, resoudreBase());

        if (!Files.exists(chemin)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable : " + nomFichier);
        }

        try {
            return Files.readAllBytes(chemin);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier", e);
        }
    }

    /**
     * Supprime tous les fichiers portant le nomBase dans le sous-répertoire (toutes extensions).
     * Opération "best-effort" : les erreurs de suppression sont ignorées.
     */
    public void supprimer(String sousRep, String nomBase) {
        Path repertoire = resoudreRepertoire(sousRep);
        supprimerExistants(repertoire, nomBase);
    }

    // ── Helpers privés ────────────────────────────────────────────────────────

    private Path resoudreBase() {
        return Path.of(uploadsDir).toAbsolutePath().normalize();
    }

    private Path resoudreRepertoire(String sousRep) {
        return resoudreBase().resolve(sousRep).normalize();
    }

    /** Protection contre les attaques path traversal. */
    private void verifierConfinement(Path chemin, Path racine) {
        if (!chemin.startsWith(racine)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chemin de fichier invalide");
        }
    }

    private void supprimerExistants(Path repertoire, String nomBase) {
        if (!Files.exists(repertoire)) return;
        try (Stream<Path> fichiers = Files.list(repertoire)) {
            fichiers
                .filter(p -> p.getFileName().toString().startsWith(nomBase + "."))
                .forEach(p -> {
                    try { Files.deleteIfExists(p); }
                    catch (IOException ignore) { /* best-effort */ }
                });
        } catch (IOException ignore) { /* best-effort */ }
    }

    private void validerFichier(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le fichier est vide");
        }
        // N'accepte que les images
        String contentType = fichier.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Type de fichier non accepté (seules les images sont autorisées)");
        }
    }

    private String obtenirExtension(String nomOriginal) {
        if (nomOriginal == null || !nomOriginal.contains(".")) return ".bin";
        return nomOriginal.substring(nomOriginal.lastIndexOf('.')).toLowerCase();
    }
}
