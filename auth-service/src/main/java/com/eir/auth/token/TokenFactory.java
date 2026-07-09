package com.eir.auth.token;

import com.eir.auth.entity.User;
import java.util.Set;

public interface TokenFactory {
    String createAccessToken(User user, Set<String> roles);
    String createRefreshToken(User user);
    TokenPair createTokenPair(User user, Set<String> roles);
}