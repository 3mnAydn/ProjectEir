package com.eir.auth;

import com.eir.auth.dto.request.LoginRequest;
import com.eir.auth.entity.Role;
import com.eir.auth.entity.User;
import com.eir.auth.exception.AuthenticationException;
import com.eir.auth.strategy.AuthenticationStrategy;
import com.eir.auth.strategy.EmailPasswordStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailPasswordStrategyComponentTest extends AbstractComponentTest {

    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        adminRole = ensureRole("ROLE_ADMIN", "System administrator");
        userRole = ensureRole("ROLE_USER", "Regular user");
    }

    @Test
    void authenticate_validCredentials_returnsAuthResult() {
        AuthenticationStrategy strategy = new EmailPasswordStrategy(userRepository, passwordEncoder);

        var request = new LoginRequest();
        request.setEmail("admin@eir.com");
        request.setPassword("Admin123!");

        var result = strategy.authenticate(request);

        assertThat(result).isNotNull();
        assertThat(result.user().getEmail()).isEqualTo("admin@eir.com");
        assertThat(result.roles()).contains("ROLE_ADMIN");
        assertThat(result.strategyType()).isEqualTo("EMAIL_PASSWORD");
    }

    @Test
    void authenticate_wrongPassword_throwsAuthenticationException() {
        AuthenticationStrategy strategy = new EmailPasswordStrategy(userRepository, passwordEncoder);

        var request = new LoginRequest();
        request.setEmail("admin@eir.com");
        request.setPassword("WrongPassword123!");

        assertThatThrownBy(() -> strategy.authenticate(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void authenticate_nonexistentEmail_throwsAuthenticationException() {
        AuthenticationStrategy strategy = new EmailPasswordStrategy(userRepository, passwordEncoder);

        var request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("Password123!");

        assertThatThrownBy(() -> strategy.authenticate(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void authenticate_disabledUser_throwsAuthenticationException() {
        User disabledUser = new User();
        disabledUser.setEmail("disabled@test.com");
        disabledUser.setPassword(passwordEncoder.encode("Password123!"));
        disabledUser.setFirstName("Disabled");
        disabledUser.setLastName("User");
        disabledUser.setIdentityNumber("99999999999");
        disabledUser.setEnabled(false);
        disabledUser.setRoles(Set.of(userRole));
        userRepository.save(disabledUser);

        AuthenticationStrategy strategy = new EmailPasswordStrategy(userRepository, passwordEncoder);

        var request = new LoginRequest();
        request.setEmail("disabled@test.com");
        request.setPassword("Password123!");

        assertThatThrownBy(() -> strategy.authenticate(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Account is disabled");
    }

    @Test
    void supports_LoginRequest_returnsTrue() {
        AuthenticationStrategy strategy = new EmailPasswordStrategy(userRepository, passwordEncoder);

        assertThat(strategy.supports(new LoginRequest())).isTrue();
    }

    @Test
    void supports_otherRequest_returnsFalse() {
        AuthenticationStrategy strategy = new EmailPasswordStrategy(userRepository, passwordEncoder);

        assertThat(strategy.supports("not a request")).isFalse();
        assertThat(strategy.supports(null)).isFalse();
        assertThat(strategy.supports(new Object())).isFalse();
    }

    @Test
    void getType_returnsEMAIL_PASSWORD() {
        AuthenticationStrategy strategy = new EmailPasswordStrategy(userRepository, passwordEncoder);

        assertThat(strategy.getType()).isEqualTo("EMAIL_PASSWORD");
    }

    @Test
    void authenticate_nonLoginRequest_throwsAuthenticationException() {
        AuthenticationStrategy strategy = new EmailPasswordStrategy(userRepository, passwordEncoder);

        assertThatThrownBy(() -> strategy.authenticate("not a LoginRequest"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid request type for email/password authentication");
    }

    @Test
    void authenticate_extractsRolesCorrectly() {
        User user = new User();
        user.setEmail("multi.role@test.com");
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setFirstName("Multi");
        user.setLastName("Role");
        user.setIdentityNumber("99999999999");
        user.setEnabled(true);

        Role adminRole = ensureRole("ROLE_ADMIN", "Admin");
        Role userRole = ensureRole("ROLE_USER", "User");
        user.setRoles(Set.of(adminRole, userRole));
        userRepository.save(user);

        var request = new LoginRequest();
        request.setEmail("multi.role@test.com");
        request.setPassword("Password123!");

        AuthenticationStrategy strategy = new EmailPasswordStrategy(userRepository, passwordEncoder);

        var result = strategy.authenticate(request);

        assertThat(result.roles()).contains("ROLE_ADMIN", "ROLE_USER");
    }
}