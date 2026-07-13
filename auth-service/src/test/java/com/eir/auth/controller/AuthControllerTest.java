package com.eir.auth.controller;

import com.eir.auth.dto.request.LoginRequest;
import com.eir.auth.dto.request.RefreshRequest;
import com.eir.auth.dto.request.RegisterRequest;
import com.eir.auth.dto.response.AuthResponse;
import com.eir.auth.dto.response.ErrorResponse;
import com.eir.auth.entity.Role;
import com.eir.auth.entity.User;
import com.eir.auth.exception.AuthenticationException;
import com.eir.auth.pipeline.AuthPipeline;
import com.eir.auth.pipeline.AuthPipelineBuilder;
import com.eir.auth.repository.UserRepository;
import com.eir.auth.token.TokenFactory;
import com.eir.common.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthPipelineBuilder pipelineBuilder;

    @Mock
    private TokenFactory tokenFactory;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserRepository userRepository;

    private AuthController authController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authController = new AuthController(pipelineBuilder, tokenFactory, jwtProvider, userRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new com.eir.auth.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_validCredentials_returns200WithAuthResponse() throws Exception {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEnabled(true);
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(Set.of(role));

        var tokens = new com.eir.auth.token.TokenPair("access-token", "refresh-token");

        // Mock pipeline to populate context with user and tokens
        com.eir.auth.pipeline.AuthPipeline mockPipeline = mock(com.eir.auth.pipeline.AuthPipeline.class);
        doAnswer(invocation -> {
            com.eir.auth.pipeline.AuthContext ctx = invocation.getArgument(0);
            ctx.setUser(user);
            ctx.setRoles(Set.of("ROLE_USER"));
            ctx.setTokens(tokens);
            return null;
        }).when(mockPipeline).execute(any());

        when(pipelineBuilder.buildLoginPipeline()).thenReturn(mockPipeline);

        // When / Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void login_invalidEmail_returns400() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid-email\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"))
                .andExpect(jsonPath("$.validationErrors[*].field").value("email"));
    }

    @Test
    void login_shortPassword_returns400() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"))
                .andExpect(jsonPath("$.validationErrors[*].field").value("password"));
    }

    @Test
    void register_validRequest_returns200WithAuthResponse() throws Exception {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEnabled(true);
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(Set.of(role));

        var tokens = new com.eir.auth.token.TokenPair("access-token", "refresh-token");

        // Mock pipeline to populate context with user and tokens
        com.eir.auth.pipeline.AuthPipeline mockPipeline = mock(com.eir.auth.pipeline.AuthPipeline.class);
        doAnswer(invocation -> {
            com.eir.auth.pipeline.AuthContext ctx = invocation.getArgument(0);
            ctx.setUser(user);
            ctx.setRoles(Set.of("ROLE_USER"));
            ctx.setTokens(tokens);
            return null;
        }).when(mockPipeline).execute(any());

        when(pipelineBuilder.buildRegisterPipeline()).thenReturn(mockPipeline);

        // When / Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"identityNumber\":\"12345678901\",\"email\":\"john@example.com\",\"password\":\"password123\",\"confirmPassword\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void register_passwordMismatch_returns400() throws Exception {
        // Given
        com.eir.auth.pipeline.AuthPipeline mockPipeline = mock(com.eir.auth.pipeline.AuthPipeline.class);
        doThrow(new com.eir.auth.exception.InvalidRequestException("Passwords do not match"))
                .when(mockPipeline).execute(any());

        when(pipelineBuilder.buildRegisterPipeline()).thenReturn(mockPipeline);

        // When / Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"identityNumber\":\"12345678901\",\"email\":\"john@example.com\",\"password\":\"password123\",\"confirmPassword\":\"different123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_request"))
                .andExpect(jsonPath("$.message").value("Passwords do not match"));
    }

    @Test
    void refresh_validToken_returns200WithNewTokens() throws Exception {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEnabled(true);
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(Set.of(role));

        var tokens = new com.eir.auth.token.TokenPair("new-access", "new-refresh");

        when(jwtProvider.getUserIdFromToken("valid-refresh")).thenReturn("1");
        when(jwtProvider.validateToken("valid-refresh")).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(tokenFactory.createTokenPair(any(), any())).thenReturn(tokens);

        // When / Then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"valid-refresh\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    void refresh_invalidToken_returns401() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"invalid-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("authentication_failed"));
    }
}