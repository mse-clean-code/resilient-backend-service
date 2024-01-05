package clc.resilient.backend.service;

import clc.resilient.backend.service.data.objects.MovieList;
import clc.resilient.backend.service.data.services.MovieListQueryService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("wiremock")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ListControllerResilienceTimeLimiterTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private MovieListQueryService movieListQueryService;

    @Test
    public void testAccountLists_TimeLimiter() {
        // Simulate a delay that exceeds the time limit
        when(movieListQueryService.getAll()).thenAnswer(invocation -> {
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
        MovieList expectedReturnMovieList = new MovieList();
        expectedReturnMovieList.setItems(new ArrayList<>());

        // Simulate a delay that exceeds the time limit
        when(movieListQueryService.add(any(MovieList.class))).thenAnswer(invocation -> {
            Thread.sleep(5000); // Adjust this delay to be longer than your time limiter's configured timeout
            return Collections.emptyList();
        });

        var requestUrl = "/tmdb/4/list";

        // Capture the start time
        long startTime = System.currentTimeMillis();

        // Perform the request
        ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, expectedReturnMovieList, String.class);

        // Capture the end time
        long endTime = System.currentTimeMillis();

        // Check the duration to see if it was cut short by the time limiter
        assertTrue(endTime - startTime < 5000, "Request should have timed out");

        // Assert the expected timeout response
        assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
    }
}
