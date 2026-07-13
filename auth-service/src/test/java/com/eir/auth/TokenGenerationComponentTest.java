package com.eir.auth;

import com.eir.auth.entity.Role;
import com.eir.auth.entity.User;
import com.eir.auth.token.TokenFactory;
import com.eir.auth.token.TokenPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TokenGenerationComponentTest extends AbstractComponentTest {

    @BeforeEach
    void setUp() {
        ensureRole("ROLE_USER", "Regular user");
        ensureRole("ROLE_ADMIN", "Admin");
        ensureRole("ROLE_DOCTOR", "Doctor");
    }

    @Test
    void createTokenPair_persistedUser_containsCorrectClaims() {
        User user = createTestUser("token.test@example.com", "Password123!", "Token", "Test", "12345678901", Set.of(ensureRole("ROLE_USER", "User"), ensureRole("ROLE_ADMIN", "Admin")));

        TokenPair pair = tokenFactory.createTokenPair(user, Set.of("ROLE_USER", "ROLE_ADMIN"));

        assertThat(pair.accessToken()).isNotNull().isNotEmpty();
        assertThat(pair.refreshToken()).isNotNull().isNotEmpty();
        assertThat(pair.accessToken()).isNotEqualTo(pair.refreshToken());

        assertThat(jwtProvider.validateToken(pair.accessToken())).isTrue();
        assertThat(jwtProvider.validateToken(pair.refreshToken())).isTrue();

        String userIdFromAccess = jwtProvider.getUserIdFromToken(pair.accessToken());
        assertThat(userIdFromAccess).isEqualTo(user.getId().toString());

        String userIdFromRefresh = jwtProvider.getUserIdFromToken(pair.refreshToken());
        assertThat(userIdFromRefresh).isEqualTo(user.getId().toString());
    }

    @Test
    void createAccessToken_includesRolesAndEmail() {
        User user = createTestUser("claims.test@example.com", "Password123!", "Claims", "Test", "12345678902", Set.of(ensureRole("ROLE_USER", "User")));

        String accessToken = tokenFactory.createAccessToken(user, Set.of("ROLE_USER", "ROLE_DOCTOR"));

        assertThat(jwtProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtProvider.getEmailFromToken(accessToken)).isEqualTo("claims.test@example.com");
        assertThat(jwtProvider.getRolesFromToken(accessToken)).contains("ROLE_USER", "ROLE_DOCTOR");
    }

    @Test
    void createRefreshToken_containsOnlyUserId() {
        User user = createTestUser("refresh.test@example.com", "Password123!", "Refresh", "Test", "12345678903", Set.of(ensureRole("ROLE_USER", "User")));

        String refreshToken = tokenFactory.createRefreshToken(user);

        assertThat(jwtProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtProvider.getUserIdFromToken(refreshToken)).isEqualTo(user.getId().toString());
        assertThat(jwtProvider.getEmailFromToken(refreshToken)).isNull();
        assertThat(jwtProvider.getRolesFromToken(refreshToken)).isNull();
    }

    @Test
    void validateToken_generatedToken_returnsTrue() {
        User user = createTestUser("validate.test@example.com", "Password123!", "Validate", "Test", "12345678904", Set.of(ensureRole("ROLE_USER", "User")));

        TokenPair pair = tokenFactory.createTokenPair(user, Set.of("ROLE_USER"));

        assertThat(jwtProvider.validateToken(pair.accessToken())).isTrue();
        assertThat(jwtProvider.validateToken(pair.refreshToken())).isTrue();
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertThat(jwtProvider.validateToken("invalid.token.here")).isFalse();
        assertThat(jwtProvider.validateToken("")).isFalse();
        assertThat(jwtProvider.validateToken(null)).isFalse();
    }

    @Test
    void getUserIdFromToken_accessToken_returnsCorrectId() {
        User user = createTestUser("userid.test@example.com", "Password123!", "UserId", "Test", "12345678905", Set.of(ensureRole("ROLE_USER", "User")));

        String accessToken = tokenFactory.createAccessToken(user, Set.of("ROLE_USER"));

        String userId = jwtProvider.getUserIdFromToken(accessToken);
        assertThat(userId).isEqualTo(user.getId().toString());
    }

    @Test
    void createTokenPair_multipleRoles_allIncluded() {
        User user = createTestUser("multi.role@example.com", "Password123!", "Multi", "Role", "55555555555", Set.of(ensureRole("ROLE_ADMIN", "Admin"), ensureRole("ROLE_USER", "User"), ensureRole("ROLE_DOCTOR", "Doctor")));

        TokenPair pair = tokenFactory.createTokenPair(user, Set.of("ROLE_ADMIN", "ROLE_USER", "ROLE_DOCTOR"));

        assertThat(jwtProvider.getRolesFromToken(pair.accessToken())).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER", "ROLE_DOCTOR");
    }

    @Test
    void accessToken_and_refreshToken_differentTokens() {
        User user = createTestUser("different.tokens@example.com", "Password123!", "Different", "Tokens", "99999999999", Set.of(ensureRole("ROLE_USER", "User")));

        TokenPair pair = tokenFactory.createTokenPair(user, Set.of("ROLE_USER"));

        assertThat(pair.accessToken()).isNotEqualTo(pair.refreshToken());
    }
}