package com.eir.auth.strategy;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class AuthenticationStrategyFactory {

    private final Map<String, AuthenticationStrategy> strategies = new ConcurrentHashMap<>();

    public AuthenticationStrategyFactory(List<AuthenticationStrategy> strategyList) {
        for (AuthenticationStrategy strategy : strategyList) {
            registerStrategy(strategy);
        }
    }

    public void registerStrategy(AuthenticationStrategy strategy) {
        strategies.put(strategy.getType(), strategy);
    }

    public AuthenticationStrategy getStrategy(String type) {
        AuthenticationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No authentication strategy found for type: " + type);
        }
        return strategy;
    }

    public AuthenticationStrategy getStrategy(Object request) {
        return strategies.values().stream()
                .filter(s -> s.supports(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No authentication strategy supports this request type"));
    }

    public Map<String, AuthenticationStrategy> getAllStrategies() {
        return Map.copyOf(strategies);
    }
}
