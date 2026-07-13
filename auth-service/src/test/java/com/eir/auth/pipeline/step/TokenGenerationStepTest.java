package com.eir.auth.pipeline.step;

import com.eir.auth.entity.Role;
import com.eir.auth.entity.User;
import com.eir.auth.pipeline.AuthContext;
import com.eir.auth.token.TokenFactory;
import com.eir.auth.token.TokenPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenGenerationStepTest {

    @Mock
    private TokenFactory tokenFactory;

    private TokenGenerationStep step;

    @BeforeEach
    void setUp() {
        step = new TokenGenerationStep(tokenFactory);
    }

    @Test
    void execute_userAndRolesPresent_setsTokens() {
        // Given
        AuthContext context = new AuthContext();
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(Set.of(role));
        context.setUser(user);
        context.setRoles(Set.of("ROLE_USER"));

        TokenPair expectedTokens = new TokenPair("access-token", "refresh-token");
        when(tokenFactory.createTokenPair(user, Set.of("ROLE_USER"))).thenReturn(expectedTokens);

        // When
        step.execute(context);

        // Then
        assertThat(context.getTokens()).isEqualTo(expectedTokens);
        verify(tokenFactory).createTokenPair(user, Set.of("ROLE_USER"));
    }

    @Test
    void execute_nullUser_throwsIllegalStateException() {
        // Given
        AuthContext context = new AuthContext();
        context.setUser(null);
        context.setRoles(Set.of("ROLE_USER"));

        // When / Then
        assertThatThrownBy(() -> step.execute(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User not set in context for token generation");
    }

    @Test
    void execute_emptyRoles_throwsIllegalStateException() {
        // Given
        AuthContext context = new AuthContext();
        User user = new User();
        user.setId(1L);
        context.setUser(user);
        context.setRoles(Set.of());

        // When / Then
        assertThatThrownBy(() -> step.execute(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Roles not set in context for token generation");
    }
}