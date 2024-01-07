package clc.resilient.backend.service.resilience;

import clc.resilient.backend.service.list.ListMapper;
import clc.resilient.backend.service.list.ListResilience;
import clc.resilient.backend.service.list.dtos.MediaItemDTO;
import clc.resilient.backend.service.list.dtos.MediaItemsDTO;
import clc.resilient.backend.service.list.dtos.MovieListDTO;
import clc.resilient.backend.service.list.dtos.ResponseDTO;
import clc.resilient.backend.service.list.entities.MovieList;
import clc.resilient.backend.service.list.entities.MediaRelation;
import clc.resilient.backend.service.list.services.DefaultMovieListService;
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
        long listId = 0L;

        MediaItemsDTO itemsDTO = new MediaItemsDTO();

        MovieList expectedMovieList = new MovieList();
        expectedMovieList.setId(listId);
        expectedMovieList.setItems(new HashSet<>());

        when(movieListQueryService.addItemsToList(eq(listId), anySet()))
                .thenThrow(new RuntimeException("Transient failure")) // First call fails
                .thenThrow(new RuntimeException("Transient failure")) // Second call fails
                .thenReturn(expectedMovieList); // Subsequent calls succeed

        when(mapper.movieListToDto(any(MovieList.class)))
                .thenReturn(new MovieListDTO(0L, null, null, null, true, null, 0, null));

        var requestUrl = "/tmdb/4/list/" + listId + "/items";

        // When
        var response = restTemplate.postForEntity(requestUrl, itemsDTO, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        // Verify that the service method was called 3 times (2 failure, one success)
        verify(movieListQueryService, times(3)).addItemsToList(eq(listId), anySet());
    }

    @Test
    void testAddItemToList_Retry_Failure() {
        // Given
        long listId = 0L;

        MediaItemsDTO itemsDTO = new MediaItemsDTO();

        when(movieListQueryService.addItemsToList(any(), any()))
                .thenThrow(new RuntimeException("Persistent failure"));

        when(mapper.movieListToDto(any(MovieList.class))).thenReturn(new MovieListDTO(0L, null, null, null, true, null, 0, null));

        var requestUrl = "/tmdb/4/list/" + listId + "/items";

        // When
        var response = restTemplate.postForEntity(requestUrl, itemsDTO, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertNotNull(response.getBody());
        // Verify that the service method was called 5 times
        verify(movieListQueryService, times(5)).addItemsToList(any(), any());
    }

    @Test
    void testUpdateList_Retry_Success() {
        // Given
        long listId = 0L;

        MovieListDTO movieListDTO = new MovieListDTO(0L, null, null, null, true, null, 0, null);

        when(mapper.movieListToEntity(any(MovieListDTO.class)))
                .thenReturn(new MovieList());

        when(movieListQueryService.updateList(any()))
                .thenThrow(new RuntimeException("Transient failure")) // First call fails
                .thenThrow(new RuntimeException("Transient failure")) // Second call fails
                .thenReturn(new MovieList()); // Subsequent calls succeed

        when(mapper.movieListToDto(any(MovieList.class)))
                .thenReturn(new MovieListDTO(0L, null, null, null, true, null, 0, null));

        var requestUrl = "/tmdb/4/list/" + listId;
        HttpEntity<MovieListDTO> requestEntity = new HttpEntity<>(movieListDTO);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                requestUrl, HttpMethod.PUT, requestEntity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        // Verify that the service method was called 3 times (2 failure, one success)
        verify(movieListQueryService, times(3)).updateList(any(MovieList.class));
    }

    @Test
    void testUpdateList_Retry_Failure() {
        // Given
        long listId = 0L;

        MovieListDTO movieListDTO = new MovieListDTO(0L, null, null, null, true, null, 0, null);

        when(mapper.movieListToEntity(any(MovieListDTO.class)))
                .thenReturn(new MovieList());
        when(movieListQueryService.updateList(any(MovieList.class)))
                .thenThrow(new RuntimeException("Persistent failure"));
        when(mapper.movieListToDto(any(MovieList.class)))
                .thenReturn(new MovieListDTO(0L, null, null, null, true, null, 0, null));

        var requestUrl = "/tmdb/4/list/" + listId;
        HttpEntity<MovieListDTO> requestEntity = new HttpEntity<>(movieListDTO);


        // When
        ResponseEntity<String> response = restTemplate.exchange(
                requestUrl, HttpMethod.PUT, requestEntity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertNotNull(response.getBody());
        // Verify that the service method was called 3 times (one failure, one success)
        verify(movieListQueryService, times(5)).updateList(any(MovieList.class));
    }

    @Test
    void testGetListDetails_Retry_Success() {
        // Given
        long listId = 1L;

        when(movieListQueryService.getWithItems(listId))
                .thenThrow(new RuntimeException("Transient failure")) // First call fails
                .thenThrow(new RuntimeException("Transient failure")) // First call fails
                .thenReturn(new MovieList()); // Subsequent calls succeed

        var requestUrl = "/tmdb/4/list/" + listId;

        // When
        var response = restTemplate.getForEntity(requestUrl, MovieList.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Verify that the service method was called twice (one failure, one success)
        verify(movieListQueryService, times(3)).getWithItems(listId);
    }

    @Test
    void testGetListDetails_Retry_Failure() {
        // Given
        long listId = 1L;
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
        MovieListDTO movieListDTO = new MovieListDTO(0L, null, null, null, true, null, 0, null);

        when(mapper.movieListToEntity(any(MovieListDTO.class)))
                .thenReturn(new MovieList());
        when(movieListQueryService.createList(any(MovieList.class)))
                .thenThrow(new RuntimeException("Transient failure")) // First two calls fail
                .thenThrow(new RuntimeException("Transient failure"))
                .thenReturn(new MovieList()); // Third call succeeds
        when(mapper.movieListToDto(any(MovieList.class)))
                .thenReturn(new MovieListDTO(0L, null, null, null, true, null, 0, null));

        var requestUrl = "/tmdb/4/list";

        // When
        var response = restTemplate.postForEntity(requestUrl, movieListDTO, String.class);


        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());

        // Verify that the service method was called three times
        verify(movieListQueryService, times(3)).createList(any(MovieList.class));
    }

    @Test
    void testCreateList_Retry_Failure() {
        // Given
        MovieListDTO movieListDTO = new MovieListDTO(0L, null, null, null, true, null, 0, null);

        when(mapper.movieListToEntity(any(MovieListDTO.class)))
                .thenReturn(new MovieList());
        when(movieListQueryService.createList(any(MovieList.class)))
                .thenThrow(new RuntimeException("Persistent failure")); // All calls fail
        when(mapper.movieListToDto(any(MovieList.class)))
                .thenReturn(new MovieListDTO(0L, null, null, null, true, null, 0, null));

        var requestUrl = "/tmdb/4/list";

        // When
        var response = restTemplate.postForEntity(requestUrl, movieListDTO, String.class);

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
        long listId = 1L;

        doThrow(new RuntimeException("Transient failure"))
                .doThrow(new RuntimeException("Transient failure"))
                .doNothing()
                .when(movieListQueryService).deleteList(listId);

        var requestUrl = "/tmdb/4/" + listId;
        HttpEntity<?> requestEntity = new HttpEntity<>(null);

        // When
        ResponseEntity<ResponseDTO> response = restTemplate.exchange(
                requestUrl, HttpMethod.DELETE, requestEntity, ResponseDTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());

        // Verify that the service method was called three times
        verify(movieListQueryService, times(3)).deleteList(listId);
    }

    @Test
    void testDeleteList_Retry_Failure() {
        // Given
        long listId = 1L;

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
        long listId = 1L;

        MediaItemsDTO itemsDTO = new MediaItemsDTO();

        when(mapper.mediaItemToEntity(any(MediaItemDTO.class)))
                .thenReturn(new MediaRelation());
        when(movieListQueryService.removeItemsFromList(any(Long.class), any()))
                .thenThrow(new RuntimeException("Transient failure")) // First two calls fail
                .thenThrow(new RuntimeException("Transient failure"))
                .thenReturn(new MovieList());
        when(mapper.movieListToDto(any(MovieList.class)))
                .thenReturn(new MovieListDTO(0L, null, null, null, true, null, 0, null));

        var requestUrl = "/tmdb/4/list/" + listId + "/items";
        HttpEntity<MediaItemsDTO> requestEntity = new HttpEntity<>(itemsDTO);

        // When
        ResponseEntity<ResponseDTO> response = restTemplate.exchange(
                requestUrl, HttpMethod.DELETE, requestEntity, ResponseDTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());

        // Verify that the service method was called
        verify(movieListQueryService, times(3)).removeItemsFromList(any(Long.class), any());
    }

    @Test
    void testRemoveItemFromList_Failure() {
        // Given
        long listId = 1L;
        MediaItemsDTO itemsDTO = new MediaItemsDTO();

        when(mapper.mediaItemToEntity(any(MediaItemDTO.class)))
                .thenReturn(new MediaRelation());
        when(movieListQueryService.removeItemsFromList(any(Long.class), any()))
                .thenThrow(new RuntimeException("Persistent failure")); // Persistent failure
        when(mapper.movieListToDto(any(MovieList.class)))
                .thenReturn(new MovieListDTO(0L, null, null, null, true, null, 0, null));

        var requestUrl = "/tmdb/4/list/" + listId + "/items";
        HttpEntity<MediaItemsDTO> requestEntity = new HttpEntity<>(itemsDTO);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                requestUrl, HttpMethod.DELETE, requestEntity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertNotNull(response.getBody());

        // Verify that the service method was called
        verify(movieListQueryService, times(5)).removeItemsFromList(any(Long.class), any());
    }
}
