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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerComponentTest extends AbstractComponentTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        adminRole = ensureRole("ROLE_ADMIN", "System administrator");
        userRole = ensureRole("ROLE_USER", "Regular user");
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
        User disabledUser = createTestUser("disabled@example.com", "Password123!", "Disabled", "User", "22222222222", Set.of(userRole));
        disabledUser.setEnabled(false);
        userRepository.save(disabledUser);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"disabled@example.com\",\"password\":\"Password123!\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("authentication_failed"));
    }

    @Test
    void login_invalidEmailFormat_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid-email\",\"password\":\"Password123!\"}"))
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
    void register_invalidIdentityNumber_returns400() throws Exception {
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
}
