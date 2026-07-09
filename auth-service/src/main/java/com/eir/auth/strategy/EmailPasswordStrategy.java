package com.eir.auth.strategy;

import com.eir.auth.dto.request.LoginRequest;
import com.eir.auth.entity.User;
import com.eir.auth.exception.AuthenticationException;
import com.eir.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.eir.auth.entity.Role;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EmailPasswordStrategy implements AuthenticationStrategy {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmailPasswordStrategy(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthenticationResult authenticate(Object request) {
        if (!(request instanceof LoginRequest loginRequest)) {
            throw new AuthenticationException("Invalid request type for email/password authentication");
        }

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new AuthenticationException("Account is disabled");
        }

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new AuthenticationResult(user, roles, getType(), java.util.Map.of());
    }

    @Override
    public boolean supports(Object request) {
        return request instanceof LoginRequest;
    }

    @Override
    public String getType() {
        return "EMAIL_PASSWORD";
    }
}
