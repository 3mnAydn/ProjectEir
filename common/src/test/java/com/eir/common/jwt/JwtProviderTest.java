package com.eir.common.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        String base64Secret = "RWlyUHJvamVjdFN1cGVyU2VjcmV0S2V5Rm9ySnd0VG9rZW5zMjAyNiEhIQ==";
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
        jwtProvider = new JwtProvider(keyBytes, 900000, 2592000000L);
    }

    private String createToken(String userId, List<String> roles) {
        return Jwts.builder()
                .subject(userId)
                .claim("roles", roles)
                .signWith(secretKey)
                .compact();
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = createToken("123", List.of("DOCTOR"));
        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
    void validateToken_withInvalidToken_returnsFalse() {
        assertFalse(jwtProvider.validateToken("invalid.jwt.token"));
    }

    @Test
    void validateToken_withNullToken_returnsFalse() {
        assertFalse(jwtProvider.validateToken(null));
    }

    @Test
    void getUserIdFromToken_withValidToken_returnsUserId() {
        String token = createToken("123", List.of("DOCTOR"));
        assertEquals("123", jwtProvider.getUserIdFromToken(token));
    }

    @Test
    void getRolesFromToken_withValidToken_returnsRoles() {
        String token = createToken("123", List.of("DOCTOR"));
        assertEquals(List.of("DOCTOR"), jwtProvider.getRolesFromToken(token));
    }

    @Test
    void createAccessToken_shouldProduceValidToken() {
        String token = jwtProvider.createAccessToken("user-1", "test@eir.com", List.of("ADMIN"));
        assertTrue(jwtProvider.validateToken(token));
        assertEquals("user-1", jwtProvider.getUserIdFromToken(token));
        assertEquals(List.of("ADMIN"), jwtProvider.getRolesFromToken(token));
    }

    @Test
    void createRefreshToken_shouldProduceValidToken() {
        String token = jwtProvider.createRefreshToken("user-1");
        assertTrue(jwtProvider.validateToken(token));
        assertEquals("user-1", jwtProvider.getUserIdFromToken(token));
    }
}
