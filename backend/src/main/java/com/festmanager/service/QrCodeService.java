package com.festmanager.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class QrCodeService {

    private static final int TAILLE_QR = 300;

    /**
     * Génère une image QR code PNG à partir d'un contenu texte.
     * Retourne les octets de l'image.
     */
    public byte[] genererQrCodeBytes(String contenu) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN, 1
            );
            BitMatrix matrice = writer.encode(contenu, BarcodeFormat.QR_CODE, TAILLE_QR, TAILLE_QR, hints);

            ByteArrayOutputStream sortie = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrice, "PNG", sortie);
            return sortie.toByteArray();

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Erreur lors de la génération du QR code", e);
        }
    }

    /**
     * Génère un QR code et retourne l'image encodée en base64.
     * Utilisé pour l'intégration directe dans les réponses JSON (data URI).
     */
    public String genererQrCodeBase64(String contenu) {
        return Base64.getEncoder().encodeToString(genererQrCodeBytes(contenu));
    }
}
