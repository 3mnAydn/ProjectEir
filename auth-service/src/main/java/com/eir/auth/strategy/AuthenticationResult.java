package com.eir.auth.strategy;

import com.eir.auth.entity.User;
import java.util.Set;

public record AuthenticationResult(
        User user,
        Set<String> roles,
        String strategyType,
        java.util.Map<String, Object> metadata
) {


}

