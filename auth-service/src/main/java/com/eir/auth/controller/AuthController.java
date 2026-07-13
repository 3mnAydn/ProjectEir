package com.eir.auth.controller;

import com.eir.auth.dto.request.LoginRequest;
import com.eir.auth.dto.request.RefreshRequest;
import com.eir.auth.dto.request.RegisterRequest;
import com.eir.auth.dto.response.AuthResponse;
import com.eir.auth.entity.User;
import com.eir.auth.pipeline.AuthContext;
import com.eir.auth.pipeline.AuthPipeline;
import com.eir.auth.pipeline.AuthPipelineBuilder;
import com.eir.auth.repository.UserRepository;
import com.eir.auth.token.TokenFactory;
import com.eir.common.jwt.JwtProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthPipelineBuilder pipelineBuilder;
    private final TokenFactory tokenFactory;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public AuthController(AuthPipelineBuilder pipelineBuilder,
                          TokenFactory tokenFactory,
                          JwtProvider jwtProvider,
                          UserRepository userRepository) {
        this.pipelineBuilder = pipelineBuilder;
        this.tokenFactory = tokenFactory;
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthContext context = new AuthContext();
        context.setRequest(request);
        context.setRegistration(false);

        AuthPipeline pipeline = pipelineBuilder.buildLoginPipeline();
        pipeline.execute(context);

        User user = context.getUser();
        AuthResponse response = new AuthResponse(
                context.getTokens().accessToken(),
                context.getTokens().refreshToken(),
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                context.getRoles()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthContext context = new AuthContext();
        context.setRequest(request);
        context.setRegistration(true);

        AuthPipeline pipeline = pipelineBuilder.buildRegisterPipeline();
        pipeline.execute(context);

        User user = context.getUser();
        AuthResponse response = new AuthResponse(
                context.getTokens().accessToken(),
                context.getTokens().refreshToken(),
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                context.getRoles()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        if (!jwtProvider.validateToken(request.getRefreshToken())) {
            throw new com.eir.auth.exception.AuthenticationException("Refresh token expired or invalid");
        }

        String userId = jwtProvider.getUserIdFromToken(request.getRefreshToken());
        if (userId == null) {
            throw new com.eir.auth.exception.AuthenticationException("Invalid refresh token");
        }

        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new com.eir.auth.exception.AuthenticationException("User not found"));

        if (!user.isEnabled()) {
            throw new com.eir.auth.exception.AuthenticationException("Account is disabled");
        }

        var roles = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());

        var tokens = tokenFactory.createTokenPair(user, roles);

        AuthResponse response = new AuthResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                roles
        );
        return ResponseEntity.ok(response);
    }
}