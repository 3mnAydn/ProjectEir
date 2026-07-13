package com.eir.auth.pipeline.step;

import com.eir.auth.pipeline.AuthContext;
import com.eir.auth.pipeline.AuthStep;
import com.eir.auth.strategy.AuthenticationResult;
import com.eir.auth.strategy.AuthenticationStrategy;
import com.eir.auth.strategy.AuthenticationStrategyFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthenticateStep implements AuthStep {

    private final AuthenticationStrategyFactory strategyFactory;

    public AuthenticateStep(AuthenticationStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    @Override
    public void execute(AuthContext context) {
        AuthenticationStrategy strategy = strategyFactory.getStrategy(context.getRequest());
        AuthenticationResult result = strategy.authenticate(context.getRequest());
        context.setAuthResult(result);
        context.setUser(result.user());
        context.setRoles(result.roles());
    }
}