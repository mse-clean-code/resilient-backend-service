package clc.resilient.backend.service;

import clc.resilient.backend.service.proxy.ProxyClient;
import clc.resilient.backend.service.proxy.ProxyController;
import clc.resilient.backend.service.proxy.ProxyResilience;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("wiremock")
public class ProxyControllerResilienceTest {

    @RegisterExtension
    static WireMockExtension TMDB_API = WireMockExtension.newInstance()
        .options(WireMockConfiguration.wireMockConfig()
            .port(9090))
        .build();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Mock
    private ProxyClient client;

    private ProxyController controller;


    @BeforeEach
    public void resetResilience4jAndWiremock() {
        // Reset resilience4j to run tests in any order
        // https://github.com/resilience4j/resilience4j/issues/1105
        var circuitBreaker = circuitBreakerRegistry
            .circuitBreaker(ProxyResilience.PROXY_CIRCUIT_BREAKER);
        circuitBreaker.reset();
        retryRegistry.remove(ProxyResilience.PROXY_RETRY);

        TMDB_API.resetRequests();
    }

    @Test
    void testTmdbApi_Success() {
        TMDB_API.stubFor(WireMock.get(urlPathMatching("/3/.*"))
            .willReturn(ok()));

        /* try {
            var test = restTemplate.getForEntity("http://localhost:9090/3/search/movie?query=godzilla&include_adult=false&language=en-US&page=1", String.class);
            System.out.println(
                test.toString()
            );
        } catch (Exception ex) {
            System.out.println(
                "hey"
            );
        } */

        var requestUrl = "/tmdb/3/search/movie?query=godzilla&include_adult=false&language=en-US&page=1";
        var response = restTemplate.getForEntity(requestUrl, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);






        /* // Initialize controller with mock client
        controller = new ProxyController(client);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String requestUri = "/3/search/movie?query=godzilla&include_adult=false&language=en-US&page=1";
        when(request.getRequestURI()).thenReturn(requestUri);

        // Use argument matchers as needed, ensure they match the method signature
        when(client.fetchTmdbApi(eq(HttpMethod.GET), eq(requestUri), any(HttpServletRequest.class), eq(null)))
                .thenReturn(ResponseEntity.ok("Success"));

        ResponseEntity<String> responseEntity = controller.tmdbApi(HttpMethod.GET, request, null, response);

        assertNotNull(responseEntity, "ResponseEntity should not be null");
        assertEquals("Success", responseEntity.getBody()); */
    }

    @Test
    void testTmdbApi_Retry_Success() {
        // Scenario: First external API request fails, but retry recover via second one!
        // Mock scenario with wiremock
        // https://stackoverflow.com/a/60006300/12347616
        TMDB_API.stubFor(WireMock.get(urlPathMatching("/3/.*"))
            .inScenario("Retry")
            .whenScenarioStateIs(STARTED)
            .willReturn(serverError())
            .willSetStateTo("First Retry"));
        TMDB_API.stubFor(WireMock.get(urlPathMatching("/3/.*"))
            .inScenario("Retry")
            .whenScenarioStateIs("First Retry")
            .willReturn(ok())
            .willSetStateTo("Second Retry"));

        var requestUrl = "/tmdb/3/search/movie?query=godzilla&include_adult=false&language=en-US&page=1";
        var response = restTemplate.getForEntity(requestUrl, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        TMDB_API.verify(2, getRequestedFor(urlPathMatching("/3/.*")));

        // // Initialize controller with mock client
        // controller = new ProxyController(client);
        //
        // HttpServletRequest request = mock(HttpServletRequest.class);
        // HttpServletResponse response = mock(HttpServletResponse.class);
        //
        // String requestUri = "/3/search/movie?query=godzilla&include_adult=false&language=en-US&page=1";
        // when(request.getRequestURI()).thenReturn(requestUri);
        //
        // // Simulate transient failure
        // when(client.fetchTmdbApi(eq(HttpMethod.GET), eq(requestUri), any(HttpServletRequest.class), eq(null)))
        //         .thenThrow(new RuntimeException("Transient failure"))
        //         .thenThrow(new RuntimeException("Transient failure"))
        //         .thenReturn(ResponseEntity.ok("Success"));
        //
        // ResponseEntity<String> responseEntity = null;
        // try {
        //     responseEntity = controller.tmdbApi(HttpMethod.GET, request, null, response);
        // } catch (RuntimeException e) {
        //     fail("Should not throw exception after retries");
        // }
        //
        // assertNotNull(responseEntity, "ResponseEntity should not be null");
        // assertEquals("Success", responseEntity.getBody());
        // verify(client, times(3)).fetchTmdbApi(eq(HttpMethod.GET), eq(requestUri), any(HttpServletRequest.class), eq(null));

        //        ResponseEntity<String> responseEntity = controller.tmdbApi(HttpMethod.GET, request, null, response);
//
//        assertNotNull(responseEntity, "ResponseEntity should not be null");
//        assertEquals("Success", responseEntity.getBody());
//        verify(client, times(3)).fetchTmdbApi(eq(HttpMethod.GET), eq(requestUri), any(HttpServletRequest.class), eq(null));
    }

    @Test
    void testTmdbApi_Retry_Failure() {
        // Scenario: All external requests fails and retry limit is exceeded
        TMDB_API.stubFor(WireMock.get(urlPathMatching("/3/.*"))
            .willReturn(serverError()));

        var requestUrl = "/tmdb/3/search/movie?query=godzilla&include_adult=false&language=en-US&page=1";
        var response = restTemplate.getForEntity(requestUrl, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isEqualTo("all retries have exhausted");
        TMDB_API.verify(3, getRequestedFor(urlPathMatching("/3/.*")));
    }



    @Test
    void testTmdbApi_CircuitBreaker1() {
        TMDB_API.stubFor(WireMock.get(urlPathMatching("/3/.*"))
            .willReturn(serverError()));

        var requestUrl = "/tmdb/3/search/movie?query=godzilla&include_adult=false&language=en-US&page=1";
        IntStream.rangeClosed(1, 5)
            .forEach(i -> {
                // Retry responses
                ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                assertThat(response.getBody()).isEqualTo("all retries have exhausted");
            });

        IntStream.rangeClosed(1, 5)
            .forEach(i -> {
                // Circuit breaker responses
                ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                assertThat(response.getBody()).isEqualTo("service is unavailable");
            });

        // Count times 3 as retry is also executed
        TMDB_API.verify(3*5, getRequestedFor(urlPathMatching("/3/.*")));


        // // Initialize controller with mock client
        // controller = new ProxyController(client);
        //
        // HttpServletRequest request = mock(HttpServletRequest.class);
        // HttpServletResponse response = mock(HttpServletResponse.class);
        //
        // String requestUri = "/3/search/movie?query=godzilla&include_adult=false&language=en-US&page=1";
        // when(request.getRequestURI()).thenReturn(requestUri);
        //
        // // Always throw an exception
        // when(client.fetchTmdbApi(eq(HttpMethod.GET), eq(requestUri), any(HttpServletRequest.class), eq(null)))
        //         .thenThrow(new RuntimeException("Persistent failure"));
        //
        // // Call the method multiple times to trigger the circuit breaker
        // for (int i = 0; i < 10; i++) {
        //     try {
        //         controller.tmdbApi(HttpMethod.GET, request, null, response);
        //     } catch (RuntimeException e) {
        //         // Assert the type of exception if needed
        //         System.out.printf(e.getMessage());
        //     }
        // }
        //
        // // Verify the number of calls to the client
        // verify(client, atMost(5)).fetchTmdbApi(eq(HttpMethod.GET), eq(requestUri), any(HttpServletRequest.class), eq(null));
        //
    }


    @Test
    void testTmdbApi_CircuitBreaker() {
        // Initialize controller with mock client
        controller = new ProxyController(client);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String requestUri = "/3/search/movie?query=godzilla&include_adult=false&language=en-US&page=1";
        when(request.getRequestURI()).thenReturn(requestUri);

        // Always throw an exception
        when(client.fetchTmdbApi(eq(HttpMethod.GET), eq(requestUri), any(HttpServletRequest.class), eq(null)))
                .thenThrow(new RuntimeException("Persistent failure"));

        // Call the method multiple times to trigger the circuit breaker
        for (int i = 0; i < 5; i++) {
            try {
                ResponseEntity<String> responseEntity = controller.tmdbApi(HttpMethod.GET, request, null, response);
                assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

            } catch (Exception ignored) {
            }
        }

        // Call the method multiple times to trigger the circuit breaker
        for (int i = 0; i < 5; i++) {
            try {
                ResponseEntity<String> responseEntity = controller.tmdbApi(HttpMethod.GET, request, null, response);
                assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            } catch (Exception ignored) {
            }
        }

        // Verify that the circuit breaker opened after the specified number of calls
        verify(client, atMost(5)).fetchTmdbApi(eq(HttpMethod.GET), eq(requestUri), any(HttpServletRequest.class), eq(null));
    }


    @Test
    void testTmdbApi_RateLimiter() {
        // Initialize controller with mock client
        controller = new ProxyController(client);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String requestUri = "/3/search/movie?query=godzilla&include_adult=false&language=en-US&page=1";
        when(request.getRequestURI()).thenReturn(requestUri);

        when(client.fetchTmdbApi(eq(HttpMethod.GET), eq(requestUri), any(HttpServletRequest.class), eq(null)))
                .thenReturn(ResponseEntity.ok("Success"));

        // Call the method multiple times rapidly
        int callCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                controller.tmdbApi(HttpMethod.GET, request, null, response);
                callCount++;
            } catch (Exception ignored) {
            }
        }

        // Depending on rate limiter configuration, adjust the expected call count
        assertTrue(callCount < 10, "Rate limiter should have limited the number of successful calls");
    }


}
