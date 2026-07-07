package com.eir.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SecurityProperties.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityPropertiesTest
{
    @Autowired
    private SecurityProperties securityProperties;

    @Test
    void shouldLoadPublicPaths()
    {
        List<String> publicPaths = securityProperties.getPublicPaths();

        assertNotNull(publicPaths);
        assertEquals(5, publicPaths.size());
        assertTrue(publicPaths.contains("/api/auth/login"));
        assertTrue(publicPaths.contains("/api/auth/refresh"));
        assertTrue(publicPaths.contains("/actuator/health"));
    }

    @Test
    void shouldLoadPrivatePaths()
    {
        List<String> privatePaths = securityProperties.getPrivatePaths();

        assertNotNull(privatePaths);
        assertEquals(5, privatePaths.size());
        assertTrue(privatePaths.contains("/api/patients/**"));
        assertTrue(privatePaths.contains("/api/appointments/**"));
    }

    @Test
    void listsShouldNeverBeNull()
    {
        assertNotNull(securityProperties.getPublicPaths());
        assertNotNull(securityProperties.getPrivatePaths());
    }
}
