package com.eir.auth.pipeline.step;

import com.eir.auth.pipeline.AuthContext;
import com.eir.auth.pipeline.AuthStep;
import com.eir.auth.strategy.AuthenticationStrategy;
import com.eir.auth.strategy.AuthenticationStrategyFactory;
import org.springframework.stereotype.Component;

@Component
public class StrategySelectionStep implements AuthStep {

    private final AuthenticationStrategyFactory strategyFactory;

    public StrategySelectionStep(AuthenticationStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    @Override
    public void execute(AuthContext context) {
        AuthenticationStrategy strategy = strategyFactory.getStrategy(context.getRequest());
        context.setAuthResult(null);
    }
}