package com.eir.auth.token;

public record TokenPair(
    String accessToken,
    String refreshToken
) {}