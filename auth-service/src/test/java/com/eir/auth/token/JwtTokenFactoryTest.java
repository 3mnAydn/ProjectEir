package com.eir.auth.token;

import com.eir.auth.entity.Role;
import com.eir.auth.entity.User;
import com.eir.common.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenFactoryTest {

    @Mock
    private JwtProvider jwtProvider;

    private JwtTokenFactory tokenFactory;

    @BeforeEach
    void setUp() {
        tokenFactory = new JwtTokenFactory(jwtProvider);
    }

    @Test
    void createAccessToken_callsJwtProviderWithCorrectParams() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(Set.of(role));

        Set<String> roles = Set.of("ROLE_USER");
        when(jwtProvider.createAccessToken("1", "test@example.com", java.util.List.of("ROLE_USER")))
                .thenReturn("access-token");

        // When
        String token = tokenFactory.createAccessToken(user, roles);

        // Then
        assertThat(token).isEqualTo("access-token");
        verify(jwtProvider).createAccessToken("1", "test@example.com", java.util.List.of("ROLE_USER"));
    }

    @Test
    void createRefreshToken_callsJwtProviderWithUserId() {
        // Given
        User user = new User();
        user.setId(42L);
        user.setEmail("test@example.com");

        when(jwtProvider.createRefreshToken("42")).thenReturn("refresh-token");

        // When
        String token = tokenFactory.createRefreshToken(user);

        // Then
        assertThat(token).isEqualTo("refresh-token");
        verify(jwtProvider).createRefreshToken("42");
    }

    @Test
    void createTokenPair_returnsPairWithBothTokens() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(Set.of(role));

        Set<String> roles = Set.of("ROLE_USER");
        when(jwtProvider.createAccessToken("1", "test@example.com", java.util.List.of("ROLE_USER")))
                .thenReturn("access-token");
        when(jwtProvider.createRefreshToken("1")).thenReturn("refresh-token");

        // When
        TokenPair pair = tokenFactory.createTokenPair(user, roles);

        // Then
        assertThat(pair).isNotNull();
        assertThat(pair.accessToken()).isEqualTo("access-token");
        assertThat(pair.refreshToken()).isEqualTo("refresh-token");
        verify(jwtProvider).createAccessToken("1", "test@example.com", java.util.List.of("ROLE_USER"));
        verify(jwtProvider).createRefreshToken("1");
    }

    @Test
    void createTokenPair_accessTokenContainsRoles() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        Role role1 = new Role();
        role1.setName("ROLE_USER");
        Role role2 = new Role();
        role2.setName("ROLE_ADMIN");
        user.setRoles(Set.of(role1, role2));

        Set<String> roles = Set.of("ROLE_USER", "ROLE_ADMIN");
        when(jwtProvider.createAccessToken(eq("1"), eq("test@example.com"), any()))
                .thenReturn("access-token");
        when(jwtProvider.createRefreshToken("1")).thenReturn("refresh-token");

        // When
        TokenPair pair = tokenFactory.createTokenPair(user, roles);

        // Then
        assertThat(pair).isNotNull();
        verify(jwtProvider).createAccessToken(eq("1"), eq("test@example.com"), any());
    }

    @Test
    void createTokenPair_refreshTokenContainsOnlyUserId() {
        // Given
        User user = new User();
        user.setId(99L);
        user.setEmail("test@example.com");

        Set<String> roles = Set.of("ROLE_USER");
        when(jwtProvider.createAccessToken(anyString(), anyString(), any())).thenReturn("access-token");
        when(jwtProvider.createRefreshToken("99")).thenReturn("refresh-token");

        // When
        TokenPair pair = tokenFactory.createTokenPair(user, roles);

        // Then
        assertThat(pair.refreshToken()).isEqualTo("refresh-token");
        verify(jwtProvider).createRefreshToken("99");
    }
}