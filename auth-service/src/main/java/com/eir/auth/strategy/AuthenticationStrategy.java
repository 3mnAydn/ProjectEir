package com.eir.auth.strategy;

public interface AuthenticationStrategy
{
    AuthenticationResult authenticate(Object request);

    boolean supports(Object request);

    String getType();

}
