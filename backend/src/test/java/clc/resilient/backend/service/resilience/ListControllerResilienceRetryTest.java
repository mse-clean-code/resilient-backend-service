package clc.resilient.backend.service.resilience;

import clc.resilient.backend.service.controllers.ListResilience;
import clc.resilient.backend.service.controllers.messages.ResponseMessage;
import clc.resilient.backend.service.controllers.messages.ResponseWithResults;
import clc.resilient.backend.service.data.objects.MovieList;
import clc.resilient.backend.service.data.objects.MovieRelation;
import clc.resilient.backend.service.data.services.MovieListQueryService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.*;
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

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("wiremock")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ListControllerResilienceRetryTest {

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
    void testAccountLists_Success() {
        // Given
        String accountId = "123";
        List<MovieList> movieLists = Arrays.asList(new MovieList(), new MovieList()); // Mock movie lists
        when(movieListQueryService.getAllWithoutItems())
                .thenThrow(new RuntimeException("Transient failure")) // First two calls fail
                .thenThrow(new RuntimeException("Transient failure"))
                .thenReturn(movieLists);

        var requestUrl = "/tmdb/4/account/" + accountId + "/lists";
        HttpEntity<String> requestEntity = new HttpEntity<>(null); // Assuming no body is needed

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                requestUrl, HttpMethod.GET, requestEntity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());

        // Verify that the service method was called
        verify(movieListQueryService, times(3)).getAllWithoutItems();
    }

    @Test
    void testAccountLists_Failure() {
        // Given
        String accountId = "123";
        when(movieListQueryService.getAllWithoutItems())
                .thenThrow(new RuntimeException("Persistent failure"));

        var requestUrl = "/tmdb/4/account/" + accountId + "/lists";
        HttpEntity<String> requestEntity = new HttpEntity<>(null); // Assuming no body is needed

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                requestUrl, HttpMethod.GET, requestEntity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertNotNull(response.getBody());

        // Verify that the service method was called
        verify(movieListQueryService, times(5)).getAllWithoutItems();
    }

    @Test
    void testAddItemToList_Retry_Success() {
        // Given
        Long listId = 0L;

        MovieList expectedReturnMovieList = new MovieList(); // Mocked return value
        expectedReturnMovieList.setId(null);
        expectedReturnMovieList.setItems(new HashSet<>());

        when(movieListQueryService.createList(any(MovieList.class)))
                .thenThrow(new RuntimeException("Transient failure")) // First call fails
                .thenThrow(new RuntimeException("Transient failure")) // First call fails
                .thenReturn(new MovieList()); // Subsequent calls succeed

        var requestUrl = "/tmdb/4/list/" + listId + "/items";

        // When
        var response = restTemplate.postForEntity(requestUrl, expectedReturnMovieList, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        // Verify that the service method was called twice (one failure, one success)
        verify(movieListQueryService, times(3)).createList(any(MovieList.class));
    }

    @Test
    void testAddItemToList_Retry_Failure() {
        // Given
        Long listId = 0L;

        MovieList expectedReturnMovieList = new MovieList(); // Mocked return value
        expectedReturnMovieList.setId(null);
        expectedReturnMovieList.setItems(new HashSet<>());

        when(movieListQueryService.createList(any(MovieList.class)))
                .thenThrow(new RuntimeException("Persistent failure")); // First call fails


        var requestUrl = "/tmdb/4/list/" + listId + "/items";

        // When
        var response = restTemplate.postForEntity(requestUrl, expectedReturnMovieList, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertNotNull(response.getBody());
        // Verify that the service method was called twice (one failure, one success)
        verify(movieListQueryService, times(5)).createList(any(MovieList.class));
    }

    @Test
    void testUpdateItem_Retry_Success() {
        // Given
        Long listId = 0L;

        MovieList expectedReturnMovieList = new MovieList(); // Mocked return value
        expectedReturnMovieList.setId(null);
        expectedReturnMovieList.setItems(new HashSet<>());

        when(movieListQueryService.createList(any(MovieList.class)))
                .thenThrow(new RuntimeException("Transient failure")) // First call fails
                .thenThrow(new RuntimeException("Transient failure")) // First call fails
                .thenReturn(new MovieList()); // Subsequent calls succeed

        var requestUrl = "/tmdb/4/list/" + listId;
        HttpEntity<MovieList> requestEntity = new HttpEntity<>(expectedReturnMovieList);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                requestUrl, HttpMethod.PUT, requestEntity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        // Verify that the service method was called twice (one failure, one success)
        verify(movieListQueryService, times(3)).createList(any(MovieList.class));
    }

    @Test
    void testUpdateItem_Retry_Failure() {
        // Given
        Long listId = 0L;

        MovieList expectedReturnMovieList = new MovieList(); // Mocked return value
        expectedReturnMovieList.setId(null);
        expectedReturnMovieList.setItems(new HashSet<>());

        when(movieListQueryService.createList(any(MovieList.class)))
                .thenThrow(new RuntimeException("Persistent failure")); // First call fails


        var requestUrl = "/tmdb/4/list/" + listId;
        HttpEntity<MovieList> requestEntity = new HttpEntity<>(expectedReturnMovieList);


        // When
        ResponseEntity<String> response = restTemplate.exchange(
                requestUrl, HttpMethod.PUT, requestEntity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertNotNull(response.getBody());
        // Verify that the service method was called twice (one failure, one success)
        verify(movieListQueryService, times(5)).createList(any(MovieList.class));
    }

    @Test
    void testGetListDetails_Retry_Success() {
        // Given
        Long listId = 1L;
        when(movieListQueryService.getWithItems(listId))
                .thenThrow(new RuntimeException("Transient failure")) // First call fails
                .thenThrow(new RuntimeException("Transient failure")) // First call fails
                .thenReturn(new MovieList()); // Subsequent calls succeed

        var requestUrl = "/tmdb/4/list/" + listId;

        // When
        var response = restTemplate.getForEntity(requestUrl, MovieList.class);

        // Then
        // TODO the status code is 500 internal server error altough we return a MovieList which should return 200ok
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        // Verify that the service method was called twice (one failure, one success)
        verify(movieListQueryService, times(3)).getWithItems(listId);
    }

    @Test
    void testGetListDetails_Retry_Failure() {
        // Given
        Long listId = 1L;
        when(movieListQueryService.getWithItems(listId))
                .thenThrow(new RuntimeException("Persistent failure"));

        var requestUrl = "/tmdb/4/list/" + listId;

        // When
        var response = restTemplate.getForEntity(requestUrl, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).contains("all retries have exhausted");

        // Verify that the service method was called as per the retry configuration
        verify(movieListQueryService, times(5)).getWithItems(listId);
    }

    @Test
    void testCreateList_Retry_Success() {
        // Given
        MovieList expectedReturnMovieList = new MovieList(); // Mocked return value from service
        expectedReturnMovieList.setId(null);
        expectedReturnMovieList.setItems(new HashSet<>());

        when(movieListQueryService.createList(any(MovieList.class)))
                .thenThrow(new RuntimeException("Transient failure")) // First two calls fail
                .thenThrow(new RuntimeException("Transient failure"))
                .thenReturn(new MovieList()); // Third call succeeds

        var requestUrl = "/tmdb/4/list";

        // When
        var response = restTemplate.postForEntity(requestUrl, expectedReturnMovieList, String.class);


        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());

        // Verify that the service method was called three times
        verify(movieListQueryService, times(3)).createList(any(MovieList.class));
    }

    @Test
    void testCreateList_Retry_Failure() {
        // Given
        MovieList expectedReturnMovieList = new MovieList(); // Create a mock MovieList object for request
        expectedReturnMovieList.setId(null);
        expectedReturnMovieList.setItems(new HashSet<>());

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
    void testDeleteList_Retry_Success() {
        // Given
        Long listId = 1L;

        doThrow(new RuntimeException("Transient failure"))
                .doThrow(new RuntimeException("Transient failure"))
                .doNothing()
                .when(movieListQueryService).deleteList(listId);

        var requestUrl = "/tmdb/4/" + listId;
        HttpEntity<?> requestEntity = new HttpEntity<>(null);

        // When
        ResponseEntity<ResponseMessage> response = restTemplate.exchange(
                requestUrl, HttpMethod.DELETE, requestEntity, ResponseMessage.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());

        // Verify that the service method was called three times
        verify(movieListQueryService, times(3)).deleteList(listId);
    }

    @Test
    void testDeleteList_Retry_Failure() {
        // Given
        Long listId = 1L;

        doThrow(new RuntimeException("Persistent failure"))
                .when(movieListQueryService).deleteList(listId); // All calls throw an exception

        var requestUrl = "/tmdb/4/" + listId;
        HttpEntity<?> requestEntity = new HttpEntity<>(null);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                requestUrl, HttpMethod.DELETE, requestEntity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("all retries have exhausted"));

        // Verify that the service method was called as per the retry configuration
        verify(movieListQueryService, times(5)).deleteList(listId);
    }

    @Test
    void testRemoveItemFromList_Success() {
        // Given
        int listId = 1;
        MovieList requestBody = new MovieList(); // Mock MovieList object for request
        requestBody.setItems(new HashSet<>());
        Set<MovieRelation> removedMovies = Set.of(new MovieRelation()); // Mock return value from service

        when(movieListQueryService.deleteMovie(any(MovieList.class)))
                .thenThrow(new RuntimeException("Transient failure")) // First two calls fail
                .thenThrow(new RuntimeException("Transient failure"))
                .thenReturn(removedMovies);

        var requestUrl = "/tmdb/4/list/" + listId + "/items";
        HttpEntity<MovieList> requestEntity = new HttpEntity<>(requestBody);

        // When
        ResponseEntity<ResponseWithResults> response = restTemplate.exchange(
                requestUrl, HttpMethod.DELETE, requestEntity, ResponseWithResults.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());

        // Verify that the service method was called
        verify(movieListQueryService, times(3)).deleteMovie(any(MovieList.class));
    }

    @Test
    void testRemoveItemFromList_Failure() {
        // Given
        int listId = 1;
        MovieList requestBody = new MovieList(); // Mock MovieList object for request
        requestBody.setItems(new HashSet<>());

        when(movieListQueryService.deleteMovie(any(MovieList.class)))
                .thenThrow(new RuntimeException("Persistent failure")); // Persistent failure

        var requestUrl = "/tmdb/4/list/" + listId + "/items";
        HttpEntity<MovieList> requestEntity = new HttpEntity<>(requestBody);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                requestUrl, HttpMethod.DELETE, requestEntity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertNotNull(response.getBody());

        // Verify that the service method was called
        verify(movieListQueryService, times(5)).deleteMovie(any(MovieList.class));
    }
}
