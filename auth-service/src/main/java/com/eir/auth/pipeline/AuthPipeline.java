package com.eir.auth.pipeline;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AuthPipeline {

    private final List<AuthStep> steps;

    public AuthPipeline(List<AuthStep> steps) {
        this.steps = steps;
    }

    public void execute(AuthContext context) {
        for (AuthStep step : steps) {
            step.execute(context);
        }
    }
}