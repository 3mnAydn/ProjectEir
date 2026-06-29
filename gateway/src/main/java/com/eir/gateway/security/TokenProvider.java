package com.eir.gateway.security;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import java.util.List;
@Component
public class TokenProvider
{

    private final SecretKey secretKey;
    public TokenProvider(@Value("${jwt.secret}") String jwtSecret) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getSigningKey()
    {
        return this.secretKey;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token)
    {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getBody().getSubject();
    }

    public List<String> getRolesFromToken(String token)
    {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getBody().get("roles",List.class);
    }

}