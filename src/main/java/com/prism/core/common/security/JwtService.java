package com.prism.core.common.security;

import com.prism.core.common.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE    = "role";
    private static final String CLAIM_PHONE   = "phone";

    @Value("${prism.jwt.secret}")
    private String secret;

    @Value("${prism.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${prism.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId, String phone, UserRole role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_USER_ID, userId.toString())
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_PHONE, phone)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiryMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_USER_ID, userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiryMs))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).get(CLAIM_USER_ID, String.class));
    }

    public UserRole extractRole(String token) {
        return UserRole.valueOf(extractAllClaims(token).get(CLAIM_ROLE, String.class));
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
