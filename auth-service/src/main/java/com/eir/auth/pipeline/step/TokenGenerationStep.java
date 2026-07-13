package com.eir.auth.pipeline.step;

import com.eir.auth.pipeline.AuthContext;
import com.eir.auth.pipeline.AuthStep;
import com.eir.auth.token.TokenFactory;
import com.eir.auth.token.TokenPair;
import org.springframework.stereotype.Component;

@Component
public class TokenGenerationStep implements AuthStep {

    private final TokenFactory tokenFactory;

    public TokenGenerationStep(TokenFactory tokenFactory) {
        this.tokenFactory = tokenFactory;
    }

    @Override
    public void execute(AuthContext context) {
        if (context.getUser() == null) {
            throw new IllegalStateException("User not set in context for token generation");
        }
        if (context.getRoles() == null || context.getRoles().isEmpty()) {
            throw new IllegalStateException("Roles not set in context for token generation");
        }

        TokenPair tokenPair = tokenFactory.createTokenPair(context.getUser(), context.getRoles());
        context.setTokens(tokenPair);
    }
}