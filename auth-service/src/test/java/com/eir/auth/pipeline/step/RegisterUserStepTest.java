package com.eir.auth.pipeline.step;

import com.eir.auth.dto.request.RegisterRequest;
import com.eir.auth.entity.Role;
import com.eir.auth.entity.User;
import com.eir.auth.exception.InvalidRequestException;
import com.eir.auth.pipeline.AuthContext;
import com.eir.auth.repository.RoleRepository;
import com.eir.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserStepTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Role userRole;

    private RegisterUserStep step;

    @BeforeEach
    void setUp() {
        step = new RegisterUserStep(userRepository, roleRepository, passwordEncoder);
        lenient().when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        lenient().when(userRole.getName()).thenReturn("ROLE_USER");
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
    }

    @Test
    void execute_validRequest_createsUserAndSetsContext() {
        // Given
        AuthContext context = new AuthContext();
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIdentityNumber("12345678901");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        request.setPhone("+905551234567");
        context.setRequest(request);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("john@example.com");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setPhone("+905551234567");
        savedUser.setEnabled(true);
        savedUser.setRoles(Set.of(userRole));

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        step.execute(context);

        // Then
        assertThat(context.getUser()).isEqualTo(savedUser);
        assertThat(context.getRoles()).containsExactly("ROLE_USER");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void execute_duplicateEmail_throwsDataIntegrityViolationException() {
        // Given
        AuthContext context = new AuthContext();
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        context.setRequest(request);

        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate email"));

        // When / Then
        assertThatThrownBy(() -> step.execute(context))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void execute_passwordMismatch_throwsInvalidRequestException() {
        // Given
        AuthContext context = new AuthContext();
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("different123");
        context.setRequest(request);

        // When / Then
        assertThatThrownBy(() -> step.execute(context))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Passwords do not match");
    }

    @Test
    void execute_nonRegisterRequest_throwsInvalidRequestException() {
        // Given
        AuthContext context = new AuthContext();
        context.setRequest(new Object());

        // When / Then
        assertThatThrownBy(() -> step.execute(context))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("RegisterRequest required for registration pipeline");
    }

    @Test
    void execute_missingDefaultRole_throwsIllegalStateException() {
        // Given
        AuthContext context = new AuthContext();
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        context.setRequest(request);

        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> step.execute(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Default role ROLE_USER not found");
    }
}