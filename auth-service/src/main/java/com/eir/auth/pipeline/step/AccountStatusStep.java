package com.eir.auth.pipeline.step;

import com.eir.auth.pipeline.AuthContext;
import com.eir.auth.pipeline.AuthStep;
import com.eir.auth.entity.User;
import com.eir.auth.exception.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class AccountStatusStep implements AuthStep {

    @Override
    public void execute(AuthContext context) {
        User user = context.getUser();
        if (user == null) {
            throw new AuthenticationException("User not found in context");
        }
        if (!user.isEnabled()) {
            throw new AuthenticationException("Account is disabled");
        }
    }
}