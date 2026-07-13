package com.eir.auth.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validToken_passes() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("valid-refresh-token");

        Set<ConstraintViolation<RefreshRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void blankToken_fails() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("");

        Set<ConstraintViolation<RefreshRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("refreshToken");
    }

    @Test
    void gettersAndSetters_work() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("test-token");

        assertThat(request.getRefreshToken()).isEqualTo("test-token");
    }
}