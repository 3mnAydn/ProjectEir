package com.eir.auth.token;

import com.eir.auth.entity.User;
import com.eir.common.jwt.JwtProvider;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtTokenFactory implements TokenFactory {

    private final JwtProvider jwtProvider;

    public JwtTokenFactory(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public String createAccessToken(User user, Set<String> roles) {
        return jwtProvider.createAccessToken(
            user.getId().toString(),
            user.getEmail(),
            roles.stream().collect(Collectors.toList())
        );
    }

    @Override
    public String createRefreshToken(User user) {
        return jwtProvider.createRefreshToken(user.getId().toString());
    }

    @Override
    public TokenPair createTokenPair(User user, Set<String> roles) {
        return new TokenPair(3
            createAccessToken(user, roles),
            createRefreshToken(user)
        );
    }
}