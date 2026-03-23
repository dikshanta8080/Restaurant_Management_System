package com.dikshanta.restaurant.management.system.group_project.service;

import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import com.dikshanta.restaurant.management.system.group_project.utils.Utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final Utils utils;

    public SecretKey generateKey() {
        String secret = utils.getJwt().getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured");
        }

        // The secret may be provided as standard Base64 OR Base64URL (which uses '-' and '_').
        // JJWT's Decoders.BASE64 will throw "Illegal base64 character: '_'" on Base64URL input.
        byte[] stream;
        try {
            stream = Decoders.BASE64.decode(secret);
        } catch (RuntimeException ignored) {
            try {
                stream = Decoders.BASE64URL.decode(secret);
            } catch (RuntimeException ignored2) {
                // Dev fallback: treat the configured string as raw key material.
                stream = secret.getBytes(StandardCharsets.UTF_8);
            }
        }

        // JJWT enforces that HMAC keys are >= 256 bits. If the configured secret string
        // isn't long enough (e.g., it's a plain dev string), derive a secure-length key.
        if (stream.length < 32) {
            try {
                stream = MessageDigest.getInstance("SHA-256").digest(stream);
            } catch (Exception e) {
                // SHA-256 should always be available; if not, fail fast with a clear message.
                throw new IllegalStateException("Unable to derive JWT signing key", e);
            }
        }

        return Keys.hmacShaKeyFor(stream);
    }

    public String getJwt(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 60 * 1000L * utils.getJwt().getExpiry()))
                .signWith(generateKey())
                .claim("Role", user.getRole())
                .claim("id", user.getId())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public boolean isExpired(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getExpiration().before(new Date(System.currentTimeMillis()));
    }

    public boolean isValid(String token, UserDetails userDetails) {
        return !isExpired(token) && userDetails.getUsername().equals(extractUsername(token));
    }
}
