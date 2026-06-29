package com.eir.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "security.paths")
public class SecurityProperties
{
    private List<String> publicPaths = new ArrayList<>();
    private List<String> privatePaths = new ArrayList<>();

    public List<String> getPublicPaths() {
        return publicPaths;
    }
    public List<String> getPrivatePaths() {
        return privatePaths;
    }

}