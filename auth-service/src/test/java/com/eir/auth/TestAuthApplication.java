package com.eir.auth;

import com.eir.common.jwt.JwtProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {"com.eir.auth"})
@EnableDiscoveryClient(autoRegister = false)
public class TestAuthApplication {

    @Bean
    public JwtProvider jwtProvider() {
        String testSecret = "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLXRlc3Rpbmc=";
        return new JwtProvider(
            testSecret,
            900000,
            2592000000L
        );
    }

    public static void main(String[] args) {
        SpringApplication.run(TestAuthApplication.class, args);
    }
}