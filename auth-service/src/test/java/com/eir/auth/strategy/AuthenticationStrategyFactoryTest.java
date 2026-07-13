package com.eir.auth.strategy;

import com.eir.auth.dto.request.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationStrategyFactoryTest {

    @Mock(lenient = true)
    private AuthenticationStrategy emailPasswordStrategy;

    private AuthenticationStrategyFactory factory;

    @BeforeEach
    void setUp() {
        when(emailPasswordStrategy.getType()).thenReturn("EMAIL_PASSWORD");
        when(emailPasswordStrategy.supports(any())).thenReturn(true);

        factory = new AuthenticationStrategyFactory(List.of(emailPasswordStrategy));
    }

    @Test
    void getStrategyByType_registeredType_returnsStrategy() {
        AuthenticationStrategy strategy = factory.getStrategy("EMAIL_PASSWORD");

        assertThat(strategy).isEqualTo(emailPasswordStrategy);
    }

    @Test
    void getStrategyByType_unknownType_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> factory.getStrategy("UNKNOWN_TYPE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No authentication strategy found for type: UNKNOWN_TYPE");
    }

    @Test
    void getStrategyByRequest_matchingStrategy_returnsStrategy() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthenticationStrategy strategy = factory.getStrategy(request);

        assertThat(strategy).isEqualTo(emailPasswordStrategy);
    }

    @Test
    void getStrategyByRequest_noMatchingStrategy_throwsIllegalArgumentException() {
        when(emailPasswordStrategy.supports(any())).thenReturn(false);

        assertThatThrownBy(() -> factory.getStrategy(new Object()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No authentication strategy supports this request type");
    }

    @Test
    void registerStrategy_newStrategy_addedToRegistry() {
        AuthenticationStrategy newStrategy = mock(AuthenticationStrategy.class);
        when(newStrategy.getType()).thenReturn("OAUTH2");

        factory.registerStrategy(newStrategy);

        AuthenticationStrategy retrieved = factory.getStrategy("OAUTH2");
        assertThat(retrieved).isEqualTo(newStrategy);
    }

    @Test
    void getAllStrategies_returnsCopyOfRegistry() {
        Map<String, AuthenticationStrategy> strategies = factory.getAllStrategies();

        assertThat(strategies).containsEntry("EMAIL_PASSWORD", emailPasswordStrategy);
        // Verify it's a copy (immutable)
        assertThatThrownBy(() -> strategies.put("TEST", mock(AuthenticationStrategy.class)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void constructor_autoRegistersBeans() {
        // The factory constructor should auto-register all provided strategies
        AuthenticationStrategy anotherStrategy = mock(AuthenticationStrategy.class);
        when(anotherStrategy.getType()).thenReturn("ANOTHER_TYPE");

        AuthenticationStrategyFactory newFactory = new AuthenticationStrategyFactory(
                List.of(emailPasswordStrategy, anotherStrategy)
        );

        assertThat(newFactory.getStrategy("EMAIL_PASSWORD")).isEqualTo(emailPasswordStrategy);
        assertThat(newFactory.getStrategy("ANOTHER_TYPE")).isEqualTo(anotherStrategy);
    }
}