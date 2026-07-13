package com.eir.auth;

import com.eir.auth.dto.request.LoginRequest;
import com.eir.auth.dto.request.RefreshRequest;
import com.eir.auth.dto.request.RegisterRequest;
import com.eir.auth.dto.response.AuthResponse;
import com.eir.auth.entity.Role;
import com.eir.auth.entity.User;
import com.eir.auth.exception.AuthenticationException;
import com.eir.auth.exception.InvalidRequestException;
import com.eir.auth.pipeline.AuthContext;
import com.eir.auth.pipeline.step.RegisterUserStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterPipelineComponentTest extends AbstractComponentTest {

    private RegisterUserStep registerUserStep;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = ensureRole("ROLE_USER", "Regular user");
        registerUserStep = new RegisterUserStep(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    void execute_validRequest_persistsUserWithRoleUser() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIdentityNumber("12345678901");
        request.setEmail("john.pipeline@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");
        request.setPhone("+905551234567");

        AuthContext context = new AuthContext();
        context.setRequest(request);

        registerUserStep.execute(context);

        User savedUser = userRepository.findByEmail("john.pipeline@example.com").orElseThrow();
        assertThat(savedUser.getFirstName()).isEqualTo("John");
        assertThat(savedUser.getLastName()).isEqualTo("Doe");
        assertThat(savedUser.getIdentityNumber()).isEqualTo("12345678901");
        assertThat(savedUser.getEmail()).isEqualTo("john.pipeline@example.com");
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles().iterator().next().getName()).isEqualTo("ROLE_USER");
        assertThat(context.getUser().getEmail()).isEqualTo(savedUser.getEmail());
        assertThat(context.getRoles()).contains("ROLE_USER");
    }

    @Test
    void execute_duplicateEmail_throwsDataIntegrityViolationException() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setIdentityNumber("12345678902");
        request.setEmail("admin@eir.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");

        AuthContext context = new AuthContext();
        context.setRequest(request);

        assertThatThrownBy(() -> registerUserStep.execute(context))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void execute_passwordMismatch_throwsInvalidRequestException() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIdentityNumber("12345678903");
        request.setEmail("john.mismatch@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Different456!");

        AuthContext context = new AuthContext();
        context.setRequest(request);

        assertThatThrownBy(() -> registerUserStep.execute(context))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Passwords do not match");
    }

    @Test
    @Transactional
    void execute_missingDefaultRole_throwsIllegalStateException() {
        roleRepository.deleteByName("ROLE_USER");

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIdentityNumber("12345678904");
        request.setEmail("john.norole@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");

        AuthContext context = new AuthContext();
        context.setRequest(request);

        assertThatThrownBy(() -> registerUserStep.execute(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Default role ROLE_USER not found");
    }

    @Test
    void execute_nonRegisterRequest_throwsInvalidRequestException() {
        AuthContext context = new AuthContext();
        context.setRequest(new Object());

        assertThatThrownBy(() -> registerUserStep.execute(context))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("RegisterRequest required for registration pipeline");
    }

    @Test
    void execute_encodesPasswordCorrectly() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIdentityNumber("12345678905");
        request.setEmail("john.encoded@example.com");
        request.setPassword("RawPassword123!");
        request.setConfirmPassword("RawPassword123!");

        AuthContext context = new AuthContext();
        context.setRequest(request);

        registerUserStep.execute(context);

        User savedUser = userRepository.findByEmail("john.encoded@example.com").orElseThrow();
        assertThat(savedUser.getPassword()).isNotEqualTo("RawPassword123!");
        assertThat(passwordEncoder.matches("RawPassword123!", savedUser.getPassword())).isTrue();
    }

    @Test
    void execute_setsCreatedAtAndEnabled() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIdentityNumber("12345678906");
        request.setEmail("john.timestamps@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");

        AuthContext context = new AuthContext();
        context.setRequest(request);

        registerUserStep.execute(context);

        User savedUser = userRepository.findByEmail("john.timestamps@example.com").orElseThrow();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNull();
        assertThat(savedUser.isEnabled()).isTrue();
    }

    @Test
    void execute_assignsPhoneNumber() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIdentityNumber("12345678907");
        request.setEmail("john.phone@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");
        request.setPhone("+905559998877");

        AuthContext context = new AuthContext();
        context.setRequest(request);

        registerUserStep.execute(context);

        User savedUser = userRepository.findByEmail("john.phone@example.com").orElseThrow();
        assertThat(savedUser.getPhone()).isEqualTo("+905559998877");
    }
}