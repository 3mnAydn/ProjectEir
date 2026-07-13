package com.eir.auth.pipeline.step;

import com.eir.auth.dto.request.LoginRequest;
import com.eir.auth.entity.Role;
import com.eir.auth.entity.User;
import com.eir.auth.exception.AuthenticationException;
import com.eir.auth.pipeline.AuthContext;
import com.eir.auth.strategy.AuthenticationResult;
import com.eir.auth.strategy.AuthenticationStrategy;
import com.eir.auth.strategy.AuthenticationStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateStepTest {

    @Mock
    private AuthenticationStrategy strategy;

    private AuthenticationStrategyFactory strategyFactory;
    private AuthenticateStep step;

    @BeforeEach
    void setUp() {
        when(strategy.getType()).thenReturn("EMAIL_PASSWORD");
        when(strategy.supports(any())).thenReturn(true);
        AuthenticationStrategyFactory factory = new AuthenticationStrategyFactory(List.of(strategy));
        step = new AuthenticateStep(factory);
    }

    @Test
    void execute_validCredentials_setsUserRolesAuthResult() {
        // Given
        AuthContext context = new AuthContext();
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        context.setRequest(request);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEnabled(true);

        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(Set.of(role));

        AuthenticationResult authResult = new AuthenticationResult(user, Set.of("ROLE_USER"), "EMAIL_PASSWORD", java.util.Map.of());

        when(strategy.authenticate(request)).thenReturn(authResult);

        // When
        step.execute(context);

        // Then
        assertThat(context.getAuthResult()).isEqualTo(authResult);
        assertThat(context.getUser()).isEqualTo(user);
        assertThat(context.getRoles()).containsExactly("ROLE_USER");
    }

    @Test
    void execute_authenticationFails_throwsAuthenticationException() {
        // Given
        AuthContext context = new AuthContext();
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong-password");
        context.setRequest(request);

        when(strategy.authenticate(request)).thenThrow(new AuthenticationException("Invalid credentials"));

        // When / Then
        assertThatThrownBy(() -> step.execute(context))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");
    }
}