package com.eir.auth.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRequest_passes() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIdentityNumber("12345678901");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        request.setPhone("+905551234567");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void blankFirstName_fails() {
        RegisterRequest request = validRequest();
        request.setFirstName("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("firstName");
    }

    @Test
    void blankLastName_fails() {
        RegisterRequest request = validRequest();
        request.setLastName("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("lastName");
    }

    @Test
    void invalidIdentityNumberFormat_fails() {
        RegisterRequest request = validRequest();
        request.setIdentityNumber("123"); // Too short

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("identityNumber");
    }

    @Test
    void blankEmail_fails() {
        RegisterRequest request = validRequest();
        request.setEmail("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    void invalidEmailFormat_fails() {
        RegisterRequest request = validRequest();
        request.setEmail("invalid-email");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    void blankPassword_fails() {
        RegisterRequest request = validRequest();
        request.setPassword("");
        request.setConfirmPassword("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Both @NotBlank and @Size for password, plus @NotBlank for confirmPassword
        assertThat(violations).hasSize(3);
        assertThat(violations).extracting("propertyPath").asString().contains("password").contains("confirmPassword");
    }

    @Test
    void shortPassword_fails() {
        RegisterRequest request = validRequest();
        request.setPassword("short");
        request.setConfirmPassword("short");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("password");
    }

    @Test
    void blankConfirmPassword_fails() {
        RegisterRequest request = validRequest();
        request.setConfirmPassword("");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("confirmPassword");
    }

    @Test
    void passwordMismatch_fails() {
        RegisterRequest request = validRequest();
        request.setPassword("password123");
        request.setConfirmPassword("different456");

        // Bean validation won't catch this - it's a business logic check in RegisterUserStep
        // But we can test the DTO structure is valid
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void phoneOptional_passes() {
        RegisterRequest request = validRequest();
        request.setPhone(null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void gettersAndSetters_work() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIdentityNumber("12345678901");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        request.setPhone("+905551234567");

        assertThat(request.getFirstName()).isEqualTo("John");
        assertThat(request.getLastName()).isEqualTo("Doe");
        assertThat(request.getIdentityNumber()).isEqualTo("12345678901");
        assertThat(request.getEmail()).isEqualTo("john@example.com");
        assertThat(request.getPassword()).isEqualTo("password123");
        assertThat(request.getConfirmPassword()).isEqualTo("password123");
        assertThat(request.getPhone()).isEqualTo("+905551234567");
    }

    private RegisterRequest validRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIdentityNumber("12345678901");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        return request;
    }
}