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
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final Utils utils;

    public SecretKey generateKey() {
        byte[] stream = Decoders.BASE64.decode(utils.getJwt().getSecret());
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
