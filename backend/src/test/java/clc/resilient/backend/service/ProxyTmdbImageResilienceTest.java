package clc.resilient.backend.service;

import clc.resilient.backend.service.proxy.ProxyResilience;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("wiremock")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProxyTmdbImageResilienceTest {

    @RegisterExtension
    static WireMockExtension TMDB_IMAGE = WireMockExtension.newInstance()
        .options(WireMockConfiguration.wireMockConfig()
            .port(9090))
        .build();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    private final UrlPathPattern proxyApiPath = urlPathMatching("/t/p/.*");

    @BeforeEach
    public void resetCircuitBreakerRetryAndWiremock() {
        // Reset resilience4j to run tests in any order
        // Works for retry but oddly not for rate limiter
        // https://github.com/resilience4j/resilience4j/issues/1105
        var circuitBreaker = circuitBreakerRegistry
            .circuitBreaker(ProxyResilience.PROXY_CIRCUIT_BREAKER);
        circuitBreaker.reset();

        var retry = retryRegistry.retry(ProxyResilience.PROXY_RETRY);
        retryRegistry.remove(ProxyResilience.PROXY_RETRY);
        retryRegistry.addConfiguration(retry.getName(), retry.getRetryConfig());

        TMDB_IMAGE.resetRequests();
    }

    @AfterEach
    public void removeRateLimiter() {
        // Rate limiter cannot be reset so first tests verifies its functionality
        // After the test run (be it successfully or not) it is removed
        rateLimiterRegistry.remove(ProxyResilience.PROXY_RATE_LIMITER);
    }

    @Test
    void testTmdbImage_Success() {
        TMDB_IMAGE.stubFor(WireMock.get(proxyApiPath)
            .willReturn(ok()));

        var requestUrl = "/image.tmdb/t/p/w342/exNtEY8QUuQh9e23wSQjkPxKIU3.jpg";
        var response = restTemplate.getForEntity(requestUrl, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testTmdbImage_Retry_Success() {
        // Scenario: First external API request fails, but retry recover via second one!
        // Mock scenario with wiremock
        // https://stackoverflow.com/a/60006300/12347616
        TMDB_IMAGE.stubFor(WireMock.get(proxyApiPath)
            .inScenario("Retry")
            .whenScenarioStateIs(STARTED)
            .willReturn(serverError())
            .willSetStateTo("First Retry"));
        TMDB_IMAGE.stubFor(WireMock.get(proxyApiPath)
            .inScenario("Retry")
            .whenScenarioStateIs("First Retry")
            .willReturn(ok())
            .willSetStateTo("Second Retry"));

        var requestUrl = "/image.tmdb/t/p/w342/exNtEY8QUuQh9e23wSQjkPxKIU3.jpg";
        var response = restTemplate.getForEntity(requestUrl, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        TMDB_IMAGE.verify(2, getRequestedFor(proxyApiPath));
    }

    @Test
    void testTmdbImage_Retry_Failure() {
        // Scenario: All external requests fails and retry limit is exceeded
        TMDB_IMAGE.stubFor(WireMock.get(proxyApiPath)
            .willReturn(serverError()));

        var requestUrl = "/image.tmdb/t/p/w342/exNtEY8QUuQh9e23wSQjkPxKIU3.jpg";
        var response = restTemplate.getForEntity(requestUrl, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        TMDB_IMAGE.verify(3, getRequestedFor(proxyApiPath));
    }

    @Test
    void testTmdbImage_CircuitBreaker() {
        TMDB_IMAGE.stubFor(WireMock.get(proxyApiPath)
            .willReturn(serverError()));

        var requestUrl = "/image.tmdb/t/p/w342/exNtEY8QUuQh9e23wSQjkPxKIU3.jpg";
        IntStream.rangeClosed(1, 5)
            .forEach(i -> {
                // Retry responses
                ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            });

        IntStream.rangeClosed(1, 5)
            .forEach(i -> {
                // Circuit breaker responses
                ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            });

        // Count times 3 as retry is also executed
        TMDB_IMAGE.verify(3*5, getRequestedFor(proxyApiPath));
    }

    @Test
    @Order(1) // This first needs to run always first, see explanation in `removeRateLimiter`
    void testTmdbImage_RateLimiter() {
        TMDB_IMAGE.stubFor(WireMock.get(proxyApiPath)
            .willReturn(ok()));

        var requestUrl = "/image.tmdb/t/p/w342/exNtEY8QUuQh9e23wSQjkPxKIU3.jpg";
        Map<Integer, Integer> responseStatusCount = new ConcurrentHashMap<>();

        IntStream.rangeClosed(1, 150)
            .parallel()
            .forEach(i -> {
                ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
                int statusCode = response.getStatusCode().value();
                responseStatusCount.put(statusCode, responseStatusCount.getOrDefault(statusCode, 0) + 1);
            });

        assertTrue(responseStatusCount.containsKey(OK.value()));
    }

}
