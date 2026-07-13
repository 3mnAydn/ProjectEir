package com.eir.auth.dto.response;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthResponseTest {

    @Test
    void allFieldsSet_gettersWork() {
        AuthResponse response = new AuthResponse();
        response.setAccessToken("access-token");
        response.setRefreshToken("refresh-token");
        response.setUserId(1L);
        response.setEmail("test@example.com");
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getRoles()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void noArgsConstructor_createsInstance() {
        AuthResponse response = new AuthResponse();
        assertThat(response).isNotNull();
    }

    @Test
    void allArgsConstructor_createsInstance() {
        AuthResponse response = new AuthResponse(
                "access-token",
                "refresh-token",
                1L,
                "test@example.com",
                "John",
                "Doe",
                Set.of("ROLE_USER")
        );

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getRoles()).containsExactly("ROLE_USER");
    }
}