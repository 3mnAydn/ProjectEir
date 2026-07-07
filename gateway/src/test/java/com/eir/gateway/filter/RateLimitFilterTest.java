/*
 * =========================================================================
 * PACKAGE: com.eir.gateway.filter
 * =========================================================================
 * This test class tests the RateLimitFilter class in the "gateway/filter"
 * package. Since we are in the same package as the class under test,
 * no additional imports are needed (package-private access is sufficient).
 */
package com.eir.gateway.filter;

// GatewaySecurityContext: The context object from which the Filter retrieves the IP address.
// The Filter accesses this object via ServerWebExchange.getAttributes()
// using the "securityContext" key.
import com.eir.gateway.GatewaySecurityContext;

// @Test        : Marks a method as a JUnit 5 test method.
// @BeforeEach  : Marks a method to run automatically BEFORE each test method.
//                Common mock setups and object creation are done here.
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// GatewayFilterChain: The filter chain interface in Spring Cloud Gateway.
// Filters run sequentially; each filter either stops the request (e.g., 429)
// or passes it to the next filter via chain.filter().
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
// ReactiveRedisTemplate: Provides reactive (non-blocking) access to Redis.
// <String, String> = <Key type, Value type>.
// ReactiveValueOperations is obtained via the opsForValue() method.
import org.springframework.data.redis.core.ReactiveRedisTemplate;
// ReactiveValueOperations: Redis string value operations.
// increment() -> increments the counter (INCR),
// set() -> sets a value (SET), get() -> reads a value (GET).
import org.springframework.data.redis.core.ReactiveValueOperations;
// HttpStatus: HTTP status code enum.
// 200 OK, 404 NOT_FOUND, 429 TOO_MANY_REQUESTS, etc.
import org.springframework.http.HttpStatus;
// ServerHttpResponse: Reactive HTTP response interface.
// setStatusCode() -> sets the HTTP status code (returns boolean).
// setComplete()   -> completes the response, returns Mono<Void>.
import org.springframework.http.server.reactive.ServerHttpResponse;
// ServerWebExchange: Represents the HTTP request-response pair in WebFlux.
// getAttributes() -> returns Map<String, Object> (carries context info).
// getResponse()   -> returns ServerHttpResponse.
import org.springframework.web.server.ServerWebExchange;
// Mono: A reactive type in Project Reactor that produces 0 or 1 element.
// RateLimitFilter.filter() returns Mono<Void>.
import reactor.core.publisher.Mono;
// StepVerifier: The Reactor-test library's tool for testing reactive streams (Mono/Flux).
// We use create(mono).verifyComplete() to verify the Mono completes without error.
import reactor.test.StepVerifier;

// ----- JUnit 5 Assertion Imports (Static) -----
// assertEquals(expected, actual)    : Checks equality of two values.
// assertNotNull(object)              : Checks that the object is not null.
import static org.junit.jupiter.api.Assertions.assertEquals;

// ----- Mockito Parameter Matchers (Static) -----
// any()       : Any type of parameter.
// anyString() : Any String parameter.
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

// ----- Mockito Static Methods -----
// mock(X.class)            : Creates a fake object of type X.
// when(mock.method())      : Starts the condition "when mock.method() is called..."
// thenReturn(value)        : Specifies the value to return for the when() condition.
// thenAnswer(answer)       : Provides a dynamic answer for the when() condition.
// verify(mock)             : Verifies that the specified method of the mock was called.
// times(N)                 : Used with verify, checks it was called exactly N times.
// never()                  : Used with verify, checks it was never called.
// verifyNoMoreInteractions : Checks that no additional interactions occurred after verify.
import static org.mockito.Mockito.*;

// =========================================================================
// TEST CLASS: RateLimitFilterTest
// =========================================================================
// NOTE: No "public" before "class" -> package-private access.
// NOTE: No @SpringBootTest -> this is a pure unit test, does not start Spring Boot.
//      We test RateLimitFilter in isolation using only Mockito.
class RateLimitFilterTest
{
    // =====================================================================
    // OBJECT UNDER TEST (System Under Test - SUT)
    // =====================================================================
    // RateLimitFilter is a Spring Cloud Gateway GlobalFilter that performs
    // IP-based rate limiting using Redis.
    // Dependency: ReactiveRedisTemplate<String, String>
    // Injected via constructor, manually instantiated.
    private RateLimitFilter rateLimitFilter;

    // =====================================================================
    // MOCKS (Fake / Imitation Objects)
    // =====================================================================
    // Why do we use mocks?
    // - Connecting to a real Redis server slows down the test and makes it brittle.
    // - Sending a real HTTP request would require an integration test.
    // - With mocks, we test the filter's OWN logic in isolation.

    // ReactiveRedisTemplate: We mock all operations going to Redis.
    // Injected via constructor.
    private ReactiveRedisTemplate<String, String> redisTemplate;

    // ReactiveValueOperations: Returned from redisTemplate.opsForValue().
    // We increment the counter in Redis using the increment() method.
    private ReactiveValueOperations<String, String> valueOps;

    // ServerWebExchange: Spring interface representing the HTTP request-response pair.
    // getAttributes() -> Map (securityContext is stored here)
    // getResponse()   -> ServerHttpResponse
    private ServerWebExchange exchange;

    // GatewayFilterChain: The filter chain. When filter(exchange) is called,
    // it passes to the next filter. Should be called in normal flow, not called on 429.
    private GatewayFilterChain chain;

    // GatewaySecurityContext: Security context related to the request (IP, userId, etc).
    // Placed in the exchange's attributes with the "securityContext" key.
    private GatewaySecurityContext securityContext;

    // ServerHttpResponse: HTTP response. When rate limit is exceeded:
    //   setStatusCode(HttpStatus.TOO_MANY_REQUESTS) + setComplete()
    private ServerHttpResponse response;

    // Map: Returned from exchange.getAttributes(). Contains GatewaySecurityContext
    // with the "securityContext" key.
    // We use a real HashMap (the filter calls map.get("securityContext")).
    private java.util.Map<String, Object> attributes;

    // =====================================================================
    // @BeforeEach -> RUNS AUTOMATICALLY BEFORE EACH TEST
    // =====================================================================
    // Purpose: To prepare a fresh environment for each test.
    // - Set up all mocks from scratch.
    // - Recreate the filter under test.
    // - Each test does not affect others (isolation).
    @BeforeEach
    void setUp()
    {
        // --- 1. Create RedisTemplate and ValueOperations mocks ---
        // mock(ReactiveRedisTemplate.class): A fake object instead of Spring's real
        // RedisTemplate. No real code runs.
        redisTemplate = mock(ReactiveRedisTemplate.class);
        valueOps = mock(ReactiveValueOperations.class);

        // when(redisTemplate.opsForValue()).thenReturn(valueOps)
        // Means "when redisTemplate.opsForValue() is called, return valueOps".
        // The filter needs this for the increment() operation.
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // --- 2. Create the filter object under test ---
        // We pass the mock RedisTemplate to the real constructor.
        // Thus the filter talks to our mock, not to Redis.
        rateLimitFilter = new RateLimitFilter(redisTemplate);

        // --- 3. Create SecurityContext and assign IP ---
        // We use a real GatewaySecurityContext (POJO).
        // Test IP: "192.168.1.1"
        // This is the IP the filter will use -> Redis key is generated based on it.
        securityContext = new GatewaySecurityContext();
        securityContext.setRequestIp("192.168.1.1");

        // --- 4. Put securityContext into the Attributes Map ---
        // The filter does exchange.getAttributes().get("securityContext") to
        // retrieve the context from here. We use a real Map because the filter
        // calls map.get() internally (no mock map needed).
        attributes = new java.util.HashMap<>();
        attributes.put("securityContext", securityContext);

        // --- 5. ServerHttpResponse mock ---
        // The filter sets 429 on the response when limit is exceeded.
        // setComplete() -> returns Mono<Void>, we provide an empty Mono.
        response = mock(ServerHttpResponse.class);
        when(response.setComplete()).thenReturn(Mono.empty());

        // --- 6. ServerWebExchange mock ---
        // exchange.getAttributes() -> returns our Map
        // exchange.getResponse()   -> returns our response mock
        exchange = mock(ServerWebExchange.class);
        when(exchange.getAttributes()).thenReturn(attributes);
        when(exchange.getResponse()).thenReturn(response);

        // --- 7. GatewayFilterChain mock ---
        // When chain.filter(exchange) is called, it should return Mono.empty().
        // In normal flow, the filter passes to this; on 429, this is not called.
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    // =====================================================================
    // TEST 1: getOrder() Should Return -2
    // =====================================================================
    // getOrder() from the Ordered interface:
    //   Lower value = HIGHER priority (runs first)
    //   ContextInit  = -3 (runs first, sets up security context)
    //   RateLimitFilter = -2 (runs next, performs rate limiting check)
    //   -> Order is correct: ContextInit -> RateLimitFilter -> others
    //
    // This test does not require any mock interaction because getOrder()
    // simply returns the constant -2. This is our simplest test.
    @Test
    void getOrder_shouldReturnNegative2()
    {
        // --- A) Call the method (Act) ---
        int order = rateLimitFilter.getOrder();

        // --- B) Verify the result (Assert) ---
        // assertEquals(expected, actual):
        // "Expected -2 but got: " + order
        assertEquals(-2, order);
    }

    // =====================================================================
    // TEST 2: First Request -> Count 1, TTL SET, Request PASSES
    // =====================================================================
    // Scenario: FIRST request from this IP.
    //   - Redis INCR = 1 (first increment)
    //   - count == 1 -> EXPIRE called (60 seconds TTL)
    //   - count 1 <= 100 -> chain.filter() called, request continues
    //   - HTTP 429 is NOT returned
    //
    // Test name format: "actionPerformed_expectedResult"
    @Test
    void normalRequest_shouldSetTtlAndProceed()
    {
        // =============================================================
        // A) SETUP (Arrange / Given)
        // =============================================================
        // Setting up mock behaviors:
        //
        // when(valueOps.increment(anyString())).thenReturn(Mono.just(1L))
        //   -> "when valueOps.increment() is called, return 1"
        //   -> anyString() = regardless of which key
        //   -> Mono.just(1L) = wraps value 1 in Mono (Long type)
        //
        // NOTE: The "L" in 1L -> denotes Long type.
        // Redis INCR command returns Long.
        when(valueOps.increment(anyString())).thenReturn(Mono.just(1L));

        // expire() should return true when called (successful TTL setting)
        // any() -> any Duration object
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));

        // =============================================================
        // B) ACTION (Act / When)
        // =============================================================
        // rateLimitFilter.filter(exchange, chain) -> returns Mono<Void>.
        //
        // At this point, the filter has NOT YET RUN! In reactive programming,
        // Mono is just a RECIPE (pipeline). Actual execution is triggered
        // only by .subscribe() or StepVerifier.
        Mono<Void> result = rateLimitFilter.filter(exchange, chain);

        // =============================================================
        // C) VERIFICATION (Assert / Then)
        // =============================================================
        // StepVerifier.create(result): Subscribes to the result Mono
        // and verifies the reactive stream step by step.
        // .verifyComplete(): Verifies the Mono completes without error
        // (with onComplete). If the filter throws an error inside,
        // verifyComplete() will fail.
        StepVerifier.create(result)
                .verifyComplete();

        // =============================================================
        // D) SIDE EFFECT CONTROLS (Mock Interaction Verify)
        // =============================================================
        // verify(mock, times(N)).method(args):
        //   "Verify that mock.method(args) was called exactly N times"

        // increment() should be called 1 time (1 increment per request)
        verify(valueOps, times(1)).increment(anyString());

        // expire() should be called 1 time (count==1, so TTL is set)
        verify(redisTemplate, times(1)).expire(anyString(), any());

        // chain.filter() should be called 1 time (request continued through chain)
        verify(chain, times(1)).filter(any());

        // response.setStatusCode(429) should NEVER be called (limit not exceeded)
        verify(response, never()).setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

        // response.setComplete() should NEVER be called (request not interrupted)
        verify(response, never()).setComplete();
    }

    // =====================================================================
    // TEST 3: Below Limit Request -> Count 50, TTL NOT SET, Request PASSES
    // =====================================================================
    // Scenario: 50th request from this IP (limit 100, not exceeded yet).
    //   - Redis INCR = 50
    //   - count != 1 -> EXPIRE not called (TTL only on first request)
    //   - count 50 <= 100 -> chain.filter() called
    //
    // This test verifies the guarantee that TTL is set only on the first request.
    @Test
    void normalRequest_withinLimit_shouldProceedWithoutTtl()
    {
        // =============================================================
        // A) SETUP
        // =============================================================
        // increment returns 50 (below limit, not the first request)
        when(valueOps.increment(anyString())).thenReturn(Mono.just(50L));

        // =============================================================
        // B) ACTION
        // =============================================================
        Mono<Void> result = rateLimitFilter.filter(exchange, chain);

        // =============================================================
        // C) REACTIVE STREAM VERIFICATION
        // =============================================================
        StepVerifier.create(result)
                .verifyComplete();

        // =============================================================
        // D) SIDE EFFECT CONTROLS
        // =============================================================

        // increment should be called 1 time
        verify(valueOps, times(1)).increment(anyString());

        // expire() should NEVER be called!
        // -> Since count != 1, TTL is not set.
        // -> This proves the TTL logic works correctly.
        verify(redisTemplate, never()).expire(anyString(), any());

        // chain.filter() should be called 1 time (request passes)
        verify(chain, times(1)).filter(any());

        // 429 should NOT be set
        verify(response, never()).setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        verify(response, never()).setComplete();
    }

    // =====================================================================
    // TEST 4: Limit EXCEEDED -> Count 101, Returns 429, Request BLOCKED
    // =====================================================================
    // Scenario: 101st request from this IP (LIMIT EXCEEDED!).
    //   - Redis INCR = 101
    //   - count 101 > 100 -> rate limit EXCEEDED!
    //   - response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS) is called
    //   - response.setComplete() is called (request is terminated)
    //   - chain.filter() is NEVER called (request BLOCKED)
    //
    // This is the MOST IMPORTANT test for RateLimitFilter: it verifies that
    // the filter correctly detects limit overflow, returns 429, and blocks the request.
    @Test
    void rateLimitExceeded_shouldReturn429()
    {
        // =============================================================
        // A) SETUP
        // =============================================================
        // increment returns 101 (limit exceeded!)
        when(valueOps.increment(anyString())).thenReturn(Mono.just(101L));

        // setStatusCode(429) should return true when called
        // ServerHttpResponse.setStatusCode(HttpStatus) -> returns boolean.
        // The filter does not use this value, but the mock must record the call.
        when(response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS)).thenReturn(true);

        // =============================================================
        // B) ACTION
        // =============================================================
        Mono<Void> result = rateLimitFilter.filter(exchange, chain);

        // =============================================================
        // C) REACTIVE STREAM VERIFICATION
        // =============================================================
        StepVerifier.create(result)
                .verifyComplete();

        // =============================================================
        // D) SIDE EFFECT CONTROLS
        // =============================================================

        // increment should be called 1 time
        verify(valueOps, times(1)).increment(anyString());

        // expire should not be called (count is not 1)
        verify(redisTemplate, never()).expire(anyString(), any());

        // 429 SHOULD BE SET (TOO_MANY_REQUESTS = 429)
        verify(response, times(1)).setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

        // setComplete() should be called (response completed)
        verify(response, times(1)).setComplete();

        // chain.filter() should NEVER be called!
        // -> Request was BLOCKED by rate limit.
        // -> This is correct behavior: chain is broken when limit is exceeded.
        verify(chain, never()).filter(any());
    }
}
