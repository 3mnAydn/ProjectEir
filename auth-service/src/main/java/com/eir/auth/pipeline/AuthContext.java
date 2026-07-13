package com.eir.auth.pipeline;

import com.eir.auth.entity.User;
import com.eir.auth.strategy.AuthenticationResult;
import com.eir.auth.token.TokenPair;
import java.util.Set;

public class AuthContext {

    private Object request;
    private User user;
    private Set<String> roles;
    private AuthenticationResult authResult;
    private TokenPair tokens;
    private boolean validated = false;
    private boolean registration = false;

    public Object getRequest() {
        return request;
    }

    public void setRequest(Object request) {
        this.request = request;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public AuthenticationResult getAuthResult() {
        return authResult;
    }

    public void setAuthResult(AuthenticationResult authResult) {
        this.authResult = authResult;
    }

    public TokenPair getTokens() {
        return tokens;
    }

    public void setTokens(TokenPair tokens) {
        this.tokens = tokens;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public boolean isRegistration() {
        return registration;
    }

    public void setRegistration(boolean registration) {
        this.registration = registration;
    }
}