package com.eir.auth.pipeline;

import com.eir.auth.pipeline.step.AccountStatusStep;
import com.eir.auth.pipeline.step.AuthenticateStep;
import com.eir.auth.pipeline.step.RegisterUserStep;
import com.eir.auth.pipeline.step.StrategySelectionStep;
import com.eir.auth.pipeline.step.TokenGenerationStep;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AuthPipelineBuilder {

    private final StrategySelectionStep strategySelectionStep;
    private final AuthenticateStep authenticateStep;
    private final AccountStatusStep accountStatusStep;
    private final TokenGenerationStep tokenGenerationStep;
    private final RegisterUserStep registerUserStep;

    public AuthPipelineBuilder(StrategySelectionStep strategySelectionStep,
                               AuthenticateStep authenticateStep,
                               AccountStatusStep accountStatusStep,
                               TokenGenerationStep tokenGenerationStep,
                               RegisterUserStep registerUserStep) {
        this.strategySelectionStep = strategySelectionStep;
        this.authenticateStep = authenticateStep;
        this.accountStatusStep = accountStatusStep;
        this.tokenGenerationStep = tokenGenerationStep;
        this.registerUserStep = registerUserStep;
    }

    public AuthPipeline buildLoginPipeline() {
        return new AuthPipeline(List.of(
                strategySelectionStep,
                authenticateStep,
                accountStatusStep,
                tokenGenerationStep
        ));
    }

    public AuthPipeline buildRegisterPipeline() {
        return new AuthPipeline(List.of(
                registerUserStep,
                tokenGenerationStep
        ));
    }
}