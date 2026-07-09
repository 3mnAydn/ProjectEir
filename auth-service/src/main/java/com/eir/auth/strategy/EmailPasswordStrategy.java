package com.eir.auth.strategy;

import com.eir.auth.entity.User;
import com.eir.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class EmailPasswordStrategy implements AuthenticationStrategy
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    PasswordEncoder
    public EmailPasswordStrategy(UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public AuthenticationResult authenticate(Object result)
    {
        if ()
    }
}
