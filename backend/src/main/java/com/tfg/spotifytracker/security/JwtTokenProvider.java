package com.tfg.spotifytracker.security;

import com.tfg.spotifytracker.config.AppProperties;
import com.tfg.spotifytracker.entity.Usuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * Clase funcional: JwtTokenProvider.
 * Su objetivo es coordinar esta parte del flujo de forma sencilla.
 * Se conecta con: AppProperties.
 */
public class JwtTokenProvider {

    private final AppProperties appProperties;

    /** Ejecuta una parte concreta de la lógica de esta clase. */

    public String generateToken(Usuario usuario) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + appProperties.getJwt().getExpirationMs());

        return Jwts.builder()
            .subject(usuario.getSpotifyId())
            .claim("userId", usuario.getId().toString())
            .claim("email", usuario.getEmail())
            .claim("displayName", usuario.getDisplayName())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(getSigningKey())
            .compact();
    }

    /** Obtiene datos para esta parte del sistema. */

    public String getSpotifyIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /** Valida que la información cumpla lo esperado. */

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT no soportado: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT malformado: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Firma JWT inválida: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT vacío: {}", e.getMessage());
        }
        return false;
    }

    /** Convierte texto o datos en un formato utilizable. */

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /** Obtiene datos para esta parte del sistema. */

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(
                appProperties.getJwt().getSecret().getBytes()
            )
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
