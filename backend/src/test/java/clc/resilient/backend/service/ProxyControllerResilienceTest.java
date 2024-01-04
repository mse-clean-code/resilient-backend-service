package clc.resilient.backend.service;

import clc.resilient.backend.service.proxy.ProxyClient;
import clc.resilient.backend.service.proxy.ProxyController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ProxyControllerResilienceTest {

    @Mock
    private ProxyClient client;

    private ProxyController controller;

    @Test
    void testTmdbApi_Success() {
        // Initialize controller with mock client
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
        assertEquals("Success", responseEntity.getBody());
    }

    @Test
    void testTmdbApi_Retry() {
        // Initialize controller with mock client
        controller = new ProxyController(client);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String requestUri = "/3/search/movie?query=godzilla&include_adult=false&language=en-US&page=1";
        when(request.getRequestURI()).thenReturn(requestUri);

        // Simulate transient failure
        when(client.fetchTmdbApi(eq(HttpMethod.GET), eq(requestUri), any(HttpServletRequest.class), eq(null)))
                .thenThrow(new RuntimeException("Transient failure"))
                .thenThrow(new RuntimeException("Transient failure"))
                .thenReturn(ResponseEntity.ok("Success"));

        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = controller.tmdbApi(HttpMethod.GET, request, null, response);
        } catch (RuntimeException e) {
            fail("Should not throw exception after retries");
        }

        assertNotNull(responseEntity, "ResponseEntity should not be null");
        assertEquals("Success", responseEntity.getBody());
        verify(client, times(3)).fetchTmdbApi(eq(HttpMethod.GET), eq(requestUri), any(HttpServletRequest.class), eq(null));
//        ResponseEntity<String> responseEntity = controller.tmdbApi(HttpMethod.GET, request, null, response);
//
//        assertNotNull(responseEntity, "ResponseEntity should not be null");
//        assertEquals("Success", responseEntity.getBody());
//        verify(client, times(3)).fetchTmdbApi(eq(HttpMethod.GET), eq(requestUri), any(HttpServletRequest.class), eq(null));
    }


    @Test
    void testTmdbApi_CircuitBreaker1() {
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
        for (int i = 0; i < 10; i++) {
            try {
                controller.tmdbApi(HttpMethod.GET, request, null, response);
            } catch (RuntimeException e) {
                // Assert the type of exception if needed
                System.out.printf(e.getMessage());
            }
        }

        // Verify the number of calls to the client
        verify(client, atMost(5)).fetchTmdbApi(eq(HttpMethod.GET), eq(requestUri), any(HttpServletRequest.class), eq(null));
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
