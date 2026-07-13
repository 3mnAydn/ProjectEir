package com.eir.auth.pipeline.step;

import com.eir.auth.dto.request.RegisterRequest;
import com.eir.auth.entity.Role;
import com.eir.auth.entity.User;
import com.eir.auth.exception.AuthenticationException;
import com.eir.auth.exception.InvalidRequestException;
import com.eir.auth.pipeline.AuthContext;
import com.eir.auth.pipeline.AuthStep;
import com.eir.auth.repository.RoleRepository;
import com.eir.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Set;

@Component
public class RegisterUserStep implements AuthStep {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserStep(UserRepository userRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void execute(AuthContext context) {
        if (!(context.getRequest() instanceof RegisterRequest request)) {
            throw new InvalidRequestException("RegisterRequest required for registration pipeline");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Passwords do not match");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Default role ROLE_USER not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setIdentityNumber(request.getIdentityNumber());
        user.setPhone(request.getPhone());
        user.setEnabled(true);
        user.setRoles(Collections.singleton(userRole));

        User savedUser = userRepository.save(user);

        context.setUser(savedUser);
        context.setRoles(Set.of("ROLE_USER"));
        context.setAuthResult(null);
    }
}