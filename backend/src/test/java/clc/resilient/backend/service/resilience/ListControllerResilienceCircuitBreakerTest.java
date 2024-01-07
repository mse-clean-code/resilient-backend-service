package clc.resilient.backend.service.resilience;

import clc.resilient.backend.service.list.ListMapper;
import clc.resilient.backend.service.list.ListResilience;
import clc.resilient.backend.service.list.dtos.MediaItemDTO;
import clc.resilient.backend.service.list.dtos.MediaItemsDTO;
import clc.resilient.backend.service.list.dtos.MovieListDTO;
import clc.resilient.backend.service.list.entities.MediaRelation;
import clc.resilient.backend.service.list.entities.MovieList;
import clc.resilient.backend.service.list.services.DefaultMovieListService;
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

import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    private DefaultMovieListService movieListQueryService;

    @MockBean
    private ListMapper mapper;

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
        when(movieListQueryService.getAllWithoutItems())
                .thenThrow(new RuntimeException("Service failure"));

        var requestUrl = "/tmdb/4/account/" + accountId + "/lists";

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
        verify(movieListQueryService, times(5*5)).getAllWithoutItems();
    }

    @Test
    void testAddItemToList_CircuitBreaker() {
        // Given
        long listId = 0L;

        MediaItemsDTO itemsDTO = new MediaItemsDTO();

        // Simulate failure in the service method
        when(movieListQueryService.addItemsToList(any(Long.class), anySet()))
                .thenThrow(new RuntimeException("Service failure"));
        when(mapper.movieListToDto(any(MovieList.class)))
                .thenReturn(new MovieListDTO(0L, null, null, null, true, null, 0, null));

        var requestUrl = "/tmdb/4/list/" + listId + "/items";

        // First 5 calls - Retry responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, itemsDTO, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });
        System.out.println("Next 5 calls should not be called since circuitbreaker is open");
        // Next 5 calls - Circuit breaker responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, itemsDTO, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });

        // Verify that the service method was called as expected
        verify(movieListQueryService, times(5*5)).addItemsToList(any(Long.class), anySet());
    }

    @Test
    void testUpdateList_CircuitBreaker() {
        // Given
        long listId = 0L;

        MovieListDTO movieListDTO = new MovieListDTO(0L, null, null, null, true, null, 0, null);

        when(mapper.movieListToEntity(any(MovieListDTO.class)))
                .thenReturn(new MovieList());
        // Simulate failure in the service method
        when(movieListQueryService.updateList(any(MovieList.class)))
                .thenThrow(new RuntimeException("Service failure"));

        when(mapper.movieListToDto(any(MovieList.class)))
                .thenReturn(new MovieListDTO(0L, null, null, null, true, null, 0, null));

        var requestUrl = "/tmdb/4/list/" + listId;
        HttpEntity<MovieListDTO> requestEntity = new HttpEntity<>(movieListDTO);

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
        verify(movieListQueryService, times(5*5)).updateList(any(MovieList.class));
    }

    @Test
    void testGetListDetails_CircuitBreaker() {
        // Given
        long listId = 0L;

        // Simulate failure in the service method
        when(movieListQueryService.getWithItems(listId))
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
        verify(movieListQueryService, times(5*5)).getWithItems(listId);
    }

    @Test
    void testCreateList_CircuitBreaker() {
        // Given
        MovieListDTO movieListDTO = new MovieListDTO(0L, null, null, null, true, null, 0, null);

        when(mapper.movieListToEntity(any(MovieListDTO.class)))
                .thenReturn(new MovieList());
        // Simulate failure in the service method
        when(movieListQueryService.createList(any(MovieList.class)))
                .thenThrow(new RuntimeException("Service failure"));
        when(mapper.movieListToDto(any(MovieList.class)))
                .thenReturn(new MovieListDTO(0L, null, null, null, true, null, 0, null));

        var requestUrl = "/tmdb/4/list";

        // First 5 calls - Retry responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, movieListDTO, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });
        System.out.println("Next 5 calls should not be called since circuitbreaker is open");
        // Next 5 calls - Circuit breaker responses
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, movieListDTO, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });

        // Verify that the service method was called as expected
        verify(movieListQueryService, times(5*5)).createList(any(MovieList.class));
    }

    @Test
    void testDeleteList_CircuitBreaker() {
        // Given
        long listId = 0L;

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
        long listId = 0L;

        MediaItemsDTO itemsDTO = new MediaItemsDTO();

        when(mapper.mediaItemToEntity(any(MediaItemDTO.class)))
                .thenReturn(new MediaRelation());
        // Simulate failure in the service method
        when(movieListQueryService.removeItemsFromList(any(Long.class), any()))
                .thenThrow(new RuntimeException("Service failure")); // Persistent failure
        when(mapper.movieListToDto(any(MovieList.class)))
                .thenReturn(new MovieListDTO(0L, null, null, null, true, null, 0, null));

        var requestUrl = "/tmdb/4/list/" + listId + "/items";
        HttpEntity<MediaItemsDTO> requestEntity = new HttpEntity<>(itemsDTO);

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
        verify(movieListQueryService, times(5*5)).removeItemsFromList(any(Long.class), any());
    }
}
