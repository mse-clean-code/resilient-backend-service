package clc.resilient.backend.service.resilience;

import clc.resilient.backend.service.list.dtos.MovieListDTO;
import clc.resilient.backend.service.list.entities.MovieList;
import clc.resilient.backend.service.list.services.DefaultMovieListService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("wiremock")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ListControllerResilienceTimeLimiterTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private DefaultMovieListService movieListQueryService;

    @Test
    public void testAccountLists_TimeLimiter() {
        // Simulate a delay that exceeds the time limit
        when(movieListQueryService.getAllWithoutItems()).thenAnswer(invocation -> {
            Thread.sleep(5000); // Adjust this delay to be longer than your time limiter's configured timeout
            return Collections.emptyList();
        });

        String requestUrl = "/tmdb/4/account/test-account/lists";

        // Capture the start time
        long startTime = System.currentTimeMillis();

        // Perform the request
        ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);

        // Capture the end time
        long endTime = System.currentTimeMillis();

        // Check the duration to see if it was cut short by the time limiter
        assertTrue(endTime - startTime < 5000, "Request should have timed out");

        // Assert the expected timeout response
        assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
    }

    @Test
    public void testCreateList_TimeLimiter() {
        MovieListDTO movieListDTO = new MovieListDTO(0L, null, null, null, true, null, 0, null);

        // Simulate a delay that exceeds the time limit
        when(movieListQueryService.createList(any(MovieList.class))).thenAnswer(invocation -> {
            Thread.sleep(5000); // Adjust this delay to be longer than your time limiter's configured timeout
            return Collections.emptyList();
        });

        var requestUrl = "/tmdb/4/list";

        // Capture the start time
        long startTime = System.currentTimeMillis();

        // Perform the request
        ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, movieListDTO, String.class);

        // Capture the end time
        long endTime = System.currentTimeMillis();

        // Check the duration to see if it was cut short by the time limiter
        assertTrue(endTime - startTime < 5000, "Request should have timed out");

        // Assert the expected timeout response
        assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
    }
}
