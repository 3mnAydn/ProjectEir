package com.eir.auth.pipeline.step;

import com.eir.auth.pipeline.AuthContext;
import com.eir.auth.pipeline.AuthStep;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

@Component
public class ValidateInputStep implements AuthStep {

    private final Validator validator;

    public ValidateInputStep(Validator validator) {
        this.validator = validator;
    }

    @Override
    public void execute(AuthContext context) {
        if (context.getRequest() == null) {
            throw new com.eir.auth.exception.InvalidRequestException("Request body is required");
        }

        var violations = validator.validate(context.getRequest());
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Validation failed");
            throw new com.eir.auth.exception.InvalidRequestException(message);
        }

        context.setValidated(true);
    }
}