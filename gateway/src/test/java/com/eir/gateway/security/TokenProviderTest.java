package com.eir.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TokenProvider.class)
@ActiveProfiles("test")

public class TokenProviderTest
{
    private TokenProvider tokenProvider;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        // application-test.yml'deki Base64 secret'ı decode et
        String base64Secret = "RWlyUHJvamVjdFN1cGVyU2VjcmV0S2V5Rm9ySnd0VG9rZW5zMjAyNiEhIQ==";
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        secretKey = Keys.hmacShaKeyFor(keyBytes);

        // TokenProvider'ı aynı secret ile oluştur
        tokenProvider = new TokenProvider(base64Secret);
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
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void validateToken_withInvalidToken_returnsFalse() {
        assertFalse(tokenProvider.validateToken("invalid.jwt.token"));
    }

    @Test
    void validateToken_withNullToken_returnsFalse() {
        assertFalse(tokenProvider.validateToken(null));
    }

    @Test
    void getUserIdFromToken_withValidToken_returnsUserId() {
        String token = createToken("123", List.of("DOCTOR"));
        assertEquals("123", tokenProvider.getUserIdFromToken(token));
    }

    @Test
    void getRolesFromToken_withValidToken_returnsRoles()
    {
        String token = createToken("123", List.of("DOCTOR"));
        assertEquals(List.of("DOCTOR"), tokenProvider.getRolesFromToken(token));
    }

}
