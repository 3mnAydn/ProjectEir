package com.eir.auth;

import com.eir.auth.dto.request.LoginRequest;
import com.eir.auth.dto.request.RefreshRequest;
import com.eir.auth.dto.request.RegisterRequest;
import com.eir.auth.dto.response.AuthResponse;
import com.eir.auth.entity.Role;
import com.eir.auth.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthServiceComponentTest extends AbstractComponentTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        adminRole = ensureRole("ROLE_ADMIN", "System administrator");
        userRole = ensureRole("ROLE_USER", "Regular user");

        // Admin user is already created by seedDefaultData() in AbstractComponentTest
        // Just verify it exists and has correct roles
        User admin = userRepository.findByEmail("admin@eir.com").orElseThrow();
        if (!admin.getRoles().stream().map(Role::getName).toList().containsAll(Set.of("ROLE_ADMIN", "ROLE_USER"))) {
            admin.setRoles(Set.of(adminRole, userRole));
            userRepository.save(admin);
        }
    }

    @Test
    void login_validCredentials_returns200WithTokens() throws Exception {
        String body = "{\"email\":\"admin@eir.com\",\"password\":\"Admin123!\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("admin@eir.com"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.roles[*]").value(org.hamcrest.Matchers.hasItem("ROLE_ADMIN")));
    }

    @Test
    void login_invalidPassword_returns401() throws Exception {
        String body = "{\"email\":\"admin@eir.com\",\"password\":\"WrongPassword123!\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("authentication_failed"));
    }

    @Test
    void login_nonexistentEmail_returns401() throws Exception {
        String body = "{\"email\":\"nonexistent@example.com\",\"password\":\"Password123!\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("authentication_failed"));
    }

    @Test
    void login_disabledUser_returns401() throws Exception {
        User disabledUser = new User();
        disabledUser.setEmail("disabled@example.com");
        disabledUser.setPassword(passwordEncoder.encode("Password123!"));
        disabledUser.setFirstName("Disabled");
        disabledUser.setLastName("User");
        disabledUser.setIdentityNumber("22222222222");
        disabledUser.setEnabled(false);
        disabledUser.setRoles(Set.of(userRole));
        userRepository.save(disabledUser);

        String body = "{\"email\":\"disabled@example.com\",\"password\":\"Password123!\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("authentication_failed"));
    }

    @Test
    void login_invalidEmailFormat_returns400() throws Exception {
        String body = "{\"email\":\"invalid-email\",\"password\":\"Password123!\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"));
    }

    @Test
    void login_missingPassword_returns400() throws Exception {
        String body = "{\"email\":\"admin@eir.com\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"));
    }

    @Test
    void register_validRequest_returns200WithTokens() throws Exception {
        String body = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "identityNumber": "12345678902",
                    "email": "john.doe@example.com",
                    "password": "Password123!",
                    "confirmPassword": "Password123!",
                    "phone": "+905551234567"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.roles[*]").value(org.hamcrest.Matchers.hasItem("ROLE_USER")));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        String body = """
                {
                    "firstName": "Admin",
                    "lastName": "Duplicate",
                    "identityNumber": "12345678903",
                    "email": "admin@eir.com",
                    "password": "Password123!",
                    "confirmPassword": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("data_integrity_violation"));
    }

    @Test
    void register_passwordMismatch_returns400() throws Exception {
        String body = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "identityNumber": "12345678904",
                    "email": "mismatch@example.com",
                    "password": "Password123!",
                    "confirmPassword": "Different456!",
                    "phone": "+905551234567"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_request"))
                .andExpect(jsonPath("$.message").value("Passwords do not match"));
    }

    @Test
    void register_shortIdentityNumber_returns400() throws Exception {
        String body = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "identityNumber": "123",
                    "email": "shortid@example.com",
                    "password": "Password123!",
                    "confirmPassword": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"))
                .andExpect(jsonPath("$.validationErrors[*].field").value(org.hamcrest.Matchers.hasItem("identityNumber")));
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        String body = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "identityNumber": "12345678905",
                    "email": "shortpass@example.com",
                    "password": "Short1",
                    "confirmPassword": "Short1"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"))
                .andExpect(jsonPath("$.validationErrors[*].field").value(org.hamcrest.Matchers.hasItem("password")));
    }

    @Test
    void register_missingFields_returns400() throws Exception {
        String body = "{}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"))
                .andExpect(jsonPath("$.validationErrors[*].field").value(org.hamcrest.Matchers.hasItem("firstName")))
                .andExpect(jsonPath("$.validationErrors[*].field").value(org.hamcrest.Matchers.hasItem("lastName")))
                .andExpect(jsonPath("$.validationErrors[*].field").value(org.hamcrest.Matchers.hasItem("identityNumber")))
                .andExpect(jsonPath("$.validationErrors[*].field").value(org.hamcrest.Matchers.hasItem("email")))
                .andExpect(jsonPath("$.validationErrors[*].field").value(org.hamcrest.Matchers.hasItem("password")))
                .andExpect(jsonPath("$.validationErrors[*].field").value(org.hamcrest.Matchers.hasItem("confirmPassword")));
    }

    @Test
    void refresh_validToken_returnsNewTokenPair() throws Exception {
        String loginBody = "{\"email\":\"admin@eir.com\",\"password\":\"Admin123!\"}";
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(loginResponse, AuthResponse.class);
        String refreshToken = authResponse.getRefreshToken();

        String refreshBody = "{\"refreshToken\":\"" + refreshToken + "\"}";

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void refresh_invalidToken_returns401() throws Exception {
        String body = "{\"refreshToken\":\"invalid.token.here\"}";

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("authentication_failed"));
    }

    @Test
    void refresh_expiredToken_returns401() throws Exception {
        String expiredToken = jwtProvider.createRefreshToken("999");

        String body = "{\"refreshToken\":\"" + expiredToken + "\"}";

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_nonexistentUser_returns401() throws Exception {
        String fakeToken = jwtProvider.createRefreshToken("999999");
        String body = "{\"refreshToken\":\"" + fakeToken + "\"}";

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}