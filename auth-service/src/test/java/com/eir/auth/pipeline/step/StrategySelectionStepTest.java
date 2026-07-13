package com.eir.auth.pipeline.step;

import com.eir.auth.dto.request.LoginRequest;
import com.eir.auth.pipeline.AuthContext;
import com.eir.auth.strategy.AuthenticationStrategy;
import com.eir.auth.strategy.AuthenticationStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StrategySelectionStepTest {

    @Mock
    private AuthenticationStrategy strategy;

    private AuthenticationStrategyFactory strategyFactory;
    private StrategySelectionStep step;

    @BeforeEach
    void setUp() {
        when(strategy.getType()).thenReturn("EMAIL_PASSWORD");
        when(strategy.supports(any())).thenReturn(true);
        strategyFactory = new AuthenticationStrategyFactory(List.of(strategy));
        step = new StrategySelectionStep(strategyFactory);
    }

    @Test
    void execute_validRequest_setsAuthResultNull() {
        // Given
        AuthContext context = new AuthContext();
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        context.setRequest(request);

        // When
        step.execute(context);

        // Then
        assertThat(context.getAuthResult()).isNull();
    }

    @Test
    void execute_unsupportedRequest_throwsIllegalArgumentException() {
        // Given - change the mock strategy to not support the request
        when(strategy.supports(any())).thenReturn(false);

        AuthContext context = new AuthContext();
        context.setRequest(new Object());

        // When / Then
        assertThatThrownBy(() -> step.execute(context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No authentication strategy supports this request type");
    }
}