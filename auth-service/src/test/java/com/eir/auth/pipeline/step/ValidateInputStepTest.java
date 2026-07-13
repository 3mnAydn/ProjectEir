package com.eir.auth.pipeline.step;

import com.eir.auth.dto.request.LoginRequest;
import com.eir.auth.exception.InvalidRequestException;
import com.eir.auth.pipeline.AuthContext;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintViolation;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidateInputStepTest {

    @Mock
    private Validator validator;

    private ValidateInputStep step;

    @BeforeEach
    void setUp() {
        step = new ValidateInputStep(validator);
    }

    @Test
    void execute_validRequest_setsValidatedTrue() {
        // Given
        AuthContext context = new AuthContext();
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        context.setRequest(request);

        when(validator.validate(request)).thenReturn(Collections.emptySet());

        // When
        step.execute(context);

        // Then
        assertThat(context.isValidated()).isTrue();
        verify(validator).validate(request);
    }

    @Test
    void execute_nullRequest_throwsInvalidRequestException() {
        // Given
        AuthContext context = new AuthContext();
        context.setRequest(null);

        // When / Then
        assertThatThrownBy(() -> step.execute(context))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Request body is required");
    }

    @Test
    void execute_invalidRequest_throwsInvalidRequestExceptionWithMessages() {
        // Given
        AuthContext context = new AuthContext();
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");
        request.setPassword("short");
        context.setRequest(request);

        // Use real validator for this test to avoid complex mocking
        jakarta.validation.Validator realValidator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        ValidateInputStep realStep = new ValidateInputStep(realValidator);

        // When / Then
        assertThatThrownBy(() -> realStep.execute(context))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("email")
                .hasMessageContaining("password");
    }

    @Test
    void execute_validRequest_doesNotThrow() {
        // Given
        AuthContext context = new AuthContext();
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        context.setRequest(request);

        when(validator.validate(request)).thenReturn(Collections.emptySet());

        // When / Then - should not throw
        step.execute(context);
        assertThat(context.isValidated()).isTrue();
    }
}