package com.eir.common.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.access-token-expiration:900000}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration:2592000000}") long refreshTokenExpiration
    ) {
        this(Decoders.BASE64.decode(jwtSecret), accessTokenExpiration, refreshTokenExpiration);
    }

    JwtProvider(byte[] keyBytes, long accessTokenExpiration, long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    private SecretKey getSigningKey() {
        return secretKey;
    }

    public boolean validateToken(String token) {
        if (token == null) return false;
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getBody().getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getBody().get(JwtConstants.CLAIM_ROLES, List.class);
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getBody().get(JwtConstants.CLAIM_EMAIL, String.class);
    }

    public String createAccessToken(String userId, String email, List<String> roles) {
        return Jwts.builder()
                .subject(userId)
                .claim(JwtConstants.CLAIM_EMAIL, email)
                .claim(JwtConstants.CLAIM_ROLES, roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String createRefreshToken(String userId) {
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }
}
