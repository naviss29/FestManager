package com.festmanager.config;

import com.festmanager.entity.Utilisateur;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String genererToken(UserDetails userDetails) {
        var builder = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs));

        // Inclure le rôle (sans préfixe ROLE_) pour que le frontend puisse le lire
        if (userDetails instanceof Utilisateur utilisateur) {
            builder.claim("role", utilisateur.getRole().name());
            if (utilisateur.getOrganisation() != null) {
                builder.claim("organisationId", utilisateur.getOrganisation().getId().toString());
            }
        }

        return builder.signWith(getSigningKey()).compact();
    }

    public String extraireEmail(String token) {
        return extraireClaims(token).getSubject();
    }

    public boolean validerToken(String token, UserDetails userDetails) {
        final String email = extraireEmail(token);
        return email.equals(userDetails.getUsername()) && !estExpire(token);
    }

    private Claims extraireClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean estExpire(String token) {
        return extraireClaims(token).getExpiration().before(new Date());
    }
}
