package com.eir.auth.strategy;

import com.eir.auth.dto.request.LoginRequest;
import com.eir.auth.entity.Role;
import com.eir.auth.entity.User;
import com.eir.auth.exception.AuthenticationException;
import com.eir.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailPasswordStrategyTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private EmailPasswordStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new EmailPasswordStrategy(userRepository, passwordEncoder);
    }

    @Test
    void authenticate_validCredentials_returnsAuthResult() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEnabled(true);

        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(Set.of(role));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);

        // When
        var result = strategy.authenticate(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.user()).isEqualTo(user);
        assertThat(result.roles()).containsExactly("ROLE_USER");
        assertThat(result.strategyType()).isEqualTo("EMAIL_PASSWORD");
        assertThat(result.metadata()).isEmpty();
    }

    @Test
    void authenticate_invalidEmail_throwsAuthenticationException() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> strategy.authenticate(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void authenticate_wrongPassword_throwsAuthenticationException() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong-password");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setEnabled(true);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> strategy.authenticate(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void authenticate_disabledUser_throwsAuthenticationException() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setEnabled(false); // Disabled account

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> strategy.authenticate(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Account is disabled");
    }

    @Test
    void supports_LoginRequest_returnsTrue() {
        LoginRequest request = new LoginRequest();

        assertThat(strategy.supports(request)).isTrue();
    }

    @Test
    void supports_otherRequest_returnsFalse() {
        assertThat(strategy.supports("not a request")).isFalse();
        assertThat(strategy.supports(null)).isFalse();
        assertThat(strategy.supports(new Object())).isFalse();
    }

    @Test
    void getType_returnsEMAIL_PASSWORD() {
        assertThat(strategy.getType()).isEqualTo("EMAIL_PASSWORD");
    }

    @Test
    void authenticate_nonLoginRequest_throwsAuthenticationException() {
        assertThatThrownBy(() -> strategy.authenticate("not a LoginRequest"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid request type for email/password authentication");
    }
}