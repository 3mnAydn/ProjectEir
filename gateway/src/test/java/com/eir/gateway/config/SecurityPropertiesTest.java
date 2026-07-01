package com.eir.gateway.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest(classes = SecurityProperties.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
        "security.paths.public-paths[0]=/api/auth/login",
        "security.paths.public-paths[1]=/api/auth/register",
        "security.paths.private-paths[0]=/api/patients/**",
        "security.paths.private-paths[1]=/api/appointments/**"
})
public class SecurityPropertiesTest
{

}
