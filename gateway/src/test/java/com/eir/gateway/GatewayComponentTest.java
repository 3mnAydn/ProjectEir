package com.eir.gateway;

import com.eir.gateway.config.SecurityProperties;
import com.eir.gateway.filter.JwtAuthenticationFilter;
import com.eir.gateway.filter.RateLimitFilter;
import com.eir.gateway.filter.handler.HeaderInjectHandler;
import com.eir.gateway.filter.handler.PermitAllHandler;
import com.eir.gateway.filter.handler.TokenExtractHandler;
import com.eir.gateway.filter.handler.TokenValidateHandler;
import com.eir.common.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GatewayComponentTest
{
    private ContextInit contextInit;
    private RateLimitFilter rateLimitFilter;
    private JwtAuthenticationFilter jwtAuthFilter;
    private SecurityProperties securityProperties;
    private ReactiveRedisTemplate<String, String> redisTemplate;
    private ReactiveValueOperations<String, String> valueOps;
    private JwtProvider jwtProvider;
    private ServerHttpResponse response;

    private static class ChainTracker
    {
        boolean completed = false;
        void markCompleted() { completed = true; }
    }

    private static class TestGatewayFilterChain implements GatewayFilterChain
    {
        private final List<GlobalFilter> filters;
        private final int index;
        private final ChainTracker tracker;

        TestGatewayFilterChain(List<GlobalFilter> filters, int index, ChainTracker tracker)
        {
            this.filters = filters;
            this.index = index;
            this.tracker = tracker;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange)
        {
            if (index < filters.size())
            {
                GlobalFilter filter = filters.get(index);
                return filter.filter(exchange, new TestGatewayFilterChain(filters, index + 1, tracker));
            }
            tracker.markCompleted();
            return Mono.empty();
        }
    }

    @BeforeEach
    void setUp()
    {
        redisTemplate = mock(ReactiveRedisTemplate.class);
        valueOps = mock(ReactiveValueOperations.class);
        jwtProvider = mock(JwtProvider.class);
        response = mock(ServerHttpResponse.class);

        securityProperties = new SecurityProperties();
        securityProperties.getPublicPaths().addAll(List.of(
                "/api/auth/login", "/api/auth/register", "/actuator/health"
        ));
        securityProperties.getPrivatePaths().addAll(List.of(
                "/api/patients/**", "/api/appointments/**", "/api/billing/**"
        ));

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(response.setComplete()).thenReturn(Mono.empty());

        PermitAllHandler permitAllHandler = new PermitAllHandler(securityProperties);
        TokenExtractHandler tokenExtractHandler = new TokenExtractHandler();
        TokenValidateHandler tokenValidateHandler = new TokenValidateHandler(jwtProvider);
        HeaderInjectHandler headerInjectHandler = new HeaderInjectHandler(jwtProvider);

        contextInit = new ContextInit();
        rateLimitFilter = new RateLimitFilter(redisTemplate);
        jwtAuthFilter = new JwtAuthenticationFilter(
                permitAllHandler, tokenExtractHandler, tokenValidateHandler, headerInjectHandler
        );
    }

    private ServerWebExchange createExchange(String path, String authHeader, String ip)
    {
        ServerHttpRequest request = mock(ServerHttpRequest.class);

        RequestPath requestPath = mock(RequestPath.class);
        when(request.getPath()).thenReturn(requestPath);
        when(requestPath.value()).thenReturn(path);

        HttpHeaders headers = mock(HttpHeaders.class);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("Authorization")).thenReturn(authHeader);

        InetSocketAddress socketAddress = mock(InetSocketAddress.class);
        InetAddress inetAddress = mock(InetAddress.class);
        when(request.getRemoteAddress()).thenReturn(socketAddress);
        when(socketAddress.getAddress()).thenReturn(inetAddress);
        when(inetAddress.getHostAddress()).thenReturn(ip);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);

        Map<String, Object> attributes = new HashMap<>();
        when(exchange.getAttributes()).thenReturn(attributes);

        return exchange;
    }

    private List<GlobalFilter> orderedFilters()
    {
        return List.of(contextInit, rateLimitFilter, jwtAuthFilter);
    }

    @Test
    void publicPath_withinRateLimit_shouldSkipAuthAndComplete()
    {
        when(valueOps.increment(anyString())).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));

        ServerWebExchange exchange = createExchange("/api/auth/login", null, "10.0.0.1");
        ChainTracker tracker = new ChainTracker();
        TestGatewayFilterChain chain = new TestGatewayFilterChain(orderedFilters(), 0, tracker);

        StepVerifier.create(chain.filter(exchange)).verifyComplete();

        GatewaySecurityContext ctx = (GatewaySecurityContext) exchange.getAttributes().get("securityContext");
        assertNotNull(ctx);
        assertEquals("10.0.0.1", ctx.getRequestIp());

        verify(valueOps, times(1)).increment(anyString());
        verify(redisTemplate, times(1)).expire(anyString(), any());

        assertTrue(tracker.completed);
        verify(response, never()).setStatusCode(any());
    }

    @Test
    void privatePath_withValidToken_shouldAuthenticateAndComplete()
    {
        when(valueOps.increment(anyString())).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));
        when(jwtProvider.validateToken("my.jwt.token")).thenReturn(true);
        when(jwtProvider.getUserIdFromToken("my.jwt.token")).thenReturn("user-42");

        ServerWebExchange exchange = createExchange("/api/patients/list", "Bearer my.jwt.token", "10.0.0.2");
        ChainTracker tracker = new ChainTracker();
        TestGatewayFilterChain chain = new TestGatewayFilterChain(orderedFilters(), 0, tracker);

        StepVerifier.create(chain.filter(exchange)).verifyComplete();

        GatewaySecurityContext ctx = (GatewaySecurityContext) exchange.getAttributes().get("securityContext");
        assertNotNull(ctx);
        assertEquals("10.0.0.2", ctx.getRequestIp());
        assertEquals("my.jwt.token", ctx.getToken());
        assertEquals("user-42", ctx.getUserId());

        verify(valueOps, times(1)).increment(anyString());
        verify(jwtProvider, times(1)).validateToken("my.jwt.token");
        verify(jwtProvider, times(1)).getUserIdFromToken("my.jwt.token");

        assertTrue(tracker.completed);
        verify(response, never()).setStatusCode(any());
    }

    @Test
    void privatePath_withoutToken_shouldReturn401()
    {
        when(valueOps.increment(anyString())).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));

        ServerWebExchange exchange = createExchange("/api/patients/list", null, "10.0.0.3");
        ChainTracker tracker = new ChainTracker();
        TestGatewayFilterChain chain = new TestGatewayFilterChain(orderedFilters(), 0, tracker);

        StepVerifier.create(chain.filter(exchange)).verifyComplete();

        GatewaySecurityContext ctx = (GatewaySecurityContext) exchange.getAttributes().get("securityContext");
        assertNotNull(ctx);
        assertEquals("10.0.0.3", ctx.getRequestIp());

        verify(valueOps, times(1)).increment(anyString());
        verify(response, times(1)).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(response, times(1)).setComplete();
        assertFalse(tracker.completed);
    }

    @Test
    void privatePath_withInvalidToken_shouldReturn401()
    {
        when(valueOps.increment(anyString())).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));
        when(jwtProvider.validateToken("bad.token")).thenReturn(false);

        ServerWebExchange exchange = createExchange("/api/patients/list", "Bearer bad.token", "10.0.0.4");
        ChainTracker tracker = new ChainTracker();
        TestGatewayFilterChain chain = new TestGatewayFilterChain(orderedFilters(), 0, tracker);

        StepVerifier.create(chain.filter(exchange)).verifyComplete();

        verify(response, times(1)).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(response, times(1)).setComplete();
        assertFalse(tracker.completed);
    }

    @Test
    void anyPath_rateLimitExceeded_shouldReturn429BeforeAuth()
    {
        when(valueOps.increment(anyString())).thenReturn(Mono.just(101L));

        ServerWebExchange exchange = createExchange("/api/patients/list", "Bearer valid.token", "10.0.0.5");
        ChainTracker tracker = new ChainTracker();
        TestGatewayFilterChain chain = new TestGatewayFilterChain(orderedFilters(), 0, tracker);

        StepVerifier.create(chain.filter(exchange)).verifyComplete();

        verify(valueOps, times(1)).increment(anyString());
        verify(response, times(1)).setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        verify(response, times(1)).setComplete();

        verify(jwtProvider, never()).validateToken(any());
        assertFalse(tracker.completed);
    }

    @Test
    void publicPath_rateLimitExceeded_shouldReturn429BeforeAuth()
    {
        when(valueOps.increment(anyString())).thenReturn(Mono.just(150L));

        ServerWebExchange exchange = createExchange("/api/auth/login", null, "10.0.0.6");
        ChainTracker tracker = new ChainTracker();
        TestGatewayFilterChain chain = new TestGatewayFilterChain(orderedFilters(), 0, tracker);

        StepVerifier.create(chain.filter(exchange)).verifyComplete();

        verify(valueOps, times(1)).increment(anyString());
        verify(response, times(1)).setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        verify(response, times(1)).setComplete();
        assertFalse(tracker.completed);
    }

    @Test
    void filterOrder_shouldBeContextInitFirst_thenRateLimit_thenJwtAuth()
    {
        assertEquals(-3, contextInit.getOrder());
        assertEquals(-2, rateLimitFilter.getOrder());
        assertEquals(-1, jwtAuthFilter.getOrder());
    }
}
