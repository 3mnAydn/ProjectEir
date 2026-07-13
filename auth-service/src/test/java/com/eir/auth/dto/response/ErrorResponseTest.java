package com.eir.auth.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ErrorResponseTest {

    @Test
    void constructorSetsTimestamp() {
        ErrorResponse response = new ErrorResponse(
                400,
                "validation_failed",
                "Validation failed",
                "/api/auth/login",
                "req-123"
        );

        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp()).isCloseTo(LocalDateTime.now(), within(5, java.time.temporal.ChronoUnit.SECONDS));
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getError()).isEqualTo("validation_failed");
        assertThat(response.getMessage()).isEqualTo("Validation failed");
        assertThat(response.getPath()).isEqualTo("/api/auth/login");
        assertThat(response.getRequestId()).isEqualTo("req-123");
        assertThat(response.getValidationErrors()).isEmpty();
    }

    @Test
    void validationErrorConstructor_setsAllFields() {
        ErrorResponse.ValidationError error = new ErrorResponse.ValidationError("email", "Invalid email format");

        assertThat(error.getField()).isEqualTo("email");
        assertThat(error.getMessage()).isEqualTo("Invalid email format");
    }

    @Test
    void validationErrorNestedClass_gettersSettersWork() {
        ErrorResponse.ValidationError error = new ErrorResponse.ValidationError("field", "message");
        error.setField("newField");
        error.setMessage("newMessage");

        assertThat(error.getField()).isEqualTo("newField");
        assertThat(error.getMessage()).isEqualTo("newMessage");
    }

    @Test
    void defaultConstructor_setsTimestamp() {
        ErrorResponse response = new ErrorResponse();

        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void settersWork() {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(401);
        response.setError("authentication_failed");
        response.setMessage("Invalid credentials");
        response.setPath("/api/auth/login");
        response.setRequestId("req-456");
        response.setValidationErrors(List.of(new ErrorResponse.ValidationError("password", "Too short")));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getError()).isEqualTo("authentication_failed");
        assertThat(response.getMessage()).isEqualTo("Invalid credentials");
        assertThat(response.getPath()).isEqualTo("/api/auth/login");
        assertThat(response.getRequestId()).isEqualTo("req-456");
        assertThat(response.getValidationErrors()).hasSize(1);
    }
}