package com.eir.auth.pipeline;

public interface AuthStep {
    void execute(AuthContext context);
}