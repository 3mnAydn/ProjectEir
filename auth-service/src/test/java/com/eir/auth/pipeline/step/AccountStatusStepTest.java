package com.eir.auth.pipeline.step;

import com.eir.auth.entity.User;
import com.eir.auth.exception.AuthenticationException;
import com.eir.auth.pipeline.AuthContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountStatusStepTest {

    @Test
    void execute_enabledUser_passes() {
        // Given
        AuthContext context = new AuthContext();
        User user = new User();
        user.setId(1L);
        user.setEnabled(true);
        context.setUser(user);

        // When / Then - should not throw
        new AccountStatusStep().execute(context);
    }

    @Test
    void execute_disabledUser_throwsAuthenticationException() {
        // Given
        AuthContext context = new AuthContext();
        User user = new User();
        user.setId(1L);
        user.setEnabled(false);
        context.setUser(user);

        // When / Then
        assertThatThrownBy(() -> new AccountStatusStep().execute(context))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Account is disabled");
    }

    @Test
    void execute_nullUser_throwsAuthenticationException() {
        // Given
        AuthContext context = new AuthContext();
        context.setUser(null);

        // When / Then
        assertThatThrownBy(() -> new AccountStatusStep().execute(context))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("User not found in context");
    }
}