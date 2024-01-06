package clc.resilient.backend.service.resilience;

import clc.resilient.backend.service.controllers.ListResilience;
import clc.resilient.backend.service.data.objects.MovieList;
import clc.resilient.backend.service.data.services.MovieListQueryService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("wiremock")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ListControllerResilienceCircuitBreakerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockBean
    private MovieListQueryService movieListQueryService;

    @BeforeEach
    public void resetCircuitBreakerRetryAndWiremock() {
        var circuitBreaker = circuitBreakerRegistry
                .circuitBreaker(ListResilience.LIST_CIRCUIT_BREAKER);
        circuitBreaker.reset();
    }

    @Test
    void testAccountLists_CircuitBreaker() {
        String accountId = "test-account";

        // Simulate failure in the service method
        when(movieListQueryService.getAll())
                .thenThrow(new RuntimeException("Service failure"));

        var requestUrl = "/tmdb/4/account/" + accountId + "/lists";

        // First 5 calls - Retry responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            // Check the specific body message for retry
        });
        System.out.println("Next 5 calls should not be called since circuitbreaker is open");
        // Next 5 calls - Circuit breaker responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            // Check the specific body message for circuit breaker
        });

        // Verify that the service method was called as expected
        verify(movieListQueryService, times(5*5)).getAll();
    }

    @Test
    void testAddItemToList_CircuitBreaker() {
        // Given
        Long listId = 0L;

        MovieList expectedReturnMovieList = new MovieList(); // Mocked return value
        expectedReturnMovieList.setItems(new ArrayList<>());

        // Simulate failure in the service method
        when(movieListQueryService.createList(any(MovieList.class)))
                .thenThrow(new RuntimeException("Service failure"));

        var requestUrl = "/tmdb/4/list/" + listId + "/items";

        // First 5 calls - Retry responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, expectedReturnMovieList, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            // Check the specific body message for retry
        });
        System.out.println("Next 5 calls should not be called since circuitbreaker is open");
        // Next 5 calls - Circuit breaker responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, expectedReturnMovieList, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            // Check the specific body message for circuit breaker
        });

        // Verify that the service method was called as expected
        verify(movieListQueryService, times(5*5)).createList(any(MovieList.class));
    }

    @Test
    void testUpdateItem_CircuitBreaker() {
        // Given
        Long listId = 0L;

        MovieList expectedReturnMovieList = new MovieList(); // Mocked return value
        expectedReturnMovieList.setItems(new ArrayList<>());

        // Simulate failure in the service method
        when(movieListQueryService.createList(any(MovieList.class)))
                .thenThrow(new RuntimeException("Service failure"));

        var requestUrl = "/tmdb/4/list/" + listId;
        HttpEntity<MovieList> requestEntity = new HttpEntity<>(expectedReturnMovieList);

        // First 5 calls - Retry responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl, HttpMethod.PUT, requestEntity, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });
        System.out.println("Next 5 calls should not be called since circuitbreaker is open");
        // Next 5 calls - Circuit breaker responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl, HttpMethod.PUT, requestEntity, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });

        // Verify that the service method was called as expected
        verify(movieListQueryService, times(5*5)).createList(any(MovieList.class));
    }

    @Test
    void testGetListDetails_CircuitBreaker() {
        // Given
        Long listId = 0L;

        // Simulate failure in the service method
        when(movieListQueryService.getItem(listId))
                .thenThrow(new RuntimeException("Service failure"));

        var requestUrl = "/tmdb/4/list/" + listId;

        // First 5 calls - Retry responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });
        System.out.println("Next 5 calls should not be called since circuitbreaker is open");
        // Next 5 calls - Circuit breaker responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });

        // Verify that the service method was called as expected
        verify(movieListQueryService, times(5*5)).getItem(listId);
    }

    @Test
    void testCreateList_CircuitBreaker() {
        // Given
        MovieList movieList = new MovieList(); // Create a mock MovieList object for request
        movieList.setItems(new ArrayList<>());

        // Simulate failure in the service method
        when(movieListQueryService.createList(any(MovieList.class)))
                .thenThrow(new RuntimeException("Service failure"));

        var requestUrl = "/tmdb/4/list";

        // First 5 calls - Retry responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, movieList, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });
        System.out.println("Next 5 calls should not be called since circuitbreaker is open");
        // Next 5 calls - Circuit breaker responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, movieList, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });

        // Verify that the service method was called as expected
        verify(movieListQueryService, times(5*5)).createList(any(MovieList.class));
    }

    void testCreateList_Retry_Failure() {
        // Given
        MovieList expectedReturnMovieList = new MovieList(); // Create a mock MovieList object for request
        expectedReturnMovieList.setId(null);
        expectedReturnMovieList.setItems(new ArrayList<>());

        when(movieListQueryService.createList(any(MovieList.class)))
                .thenThrow(new RuntimeException("Persistent failure")); // All calls fail

        var requestUrl = "/tmdb/4/list";

        // When
        var response = restTemplate.postForEntity(requestUrl, expectedReturnMovieList, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("all retries have exhausted"));

        // Verify that the service method was called as per the retry configuration
        verify(movieListQueryService, times(5)).createList(any(MovieList.class));
    }
    @Test
    void testDeleteList_CircuitBreaker() {
        // Given
        Long listId = 0L;

        // Simulate failure in the service method
        doThrow(new RuntimeException("Service failure"))
                .when(movieListQueryService).deleteList(listId); // All calls throw an exception

        var requestUrl = "/tmdb/4/" + listId;
        HttpEntity<?> requestEntity = new HttpEntity<>(null);

        // First 5 calls - Retry responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl, HttpMethod.DELETE, requestEntity, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });
        System.out.println("Next 5 calls should not be called since circuitbreaker is open");
        // Next 5 calls - Circuit breaker responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl, HttpMethod.DELETE, requestEntity, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });

        // Verify that the service method was called as expected
        verify(movieListQueryService, times(5*5)).deleteList(listId);
    }

    @Test
    void testRemoveItemFromList_CircuitBreaker() {
        // Given
        Long listId = 0L;
        MovieList requestBody = new MovieList(); // Mock MovieList object for request
        requestBody.setItems(new ArrayList<>());

        // Simulate failure in the service method
        when(movieListQueryService.deleteMovie(any(MovieList.class)))
                .thenThrow(new RuntimeException("Service failure")); // Persistent failure

        var requestUrl = "/tmdb/4/list/" + listId + "/items";
        HttpEntity<MovieList> requestEntity = new HttpEntity<>(requestBody);

        // First 5 calls - Retry responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl, HttpMethod.DELETE, requestEntity, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });
        System.out.println("Next 5 calls should not be called since circuitbreaker is open");
        // Next 5 calls - Circuit breaker responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl, HttpMethod.DELETE, requestEntity, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });

        // Verify that the service method was called as expected
        verify(movieListQueryService, times(5*5)).deleteMovie(any(MovieList.class));
    }
}
