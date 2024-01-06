package clc.resilient.backend.service.integration;

import clc.resilient.backend.service.controllers.messages.ResponseMessage;
import clc.resilient.backend.service.data.repositories.MovieListRepository;
import clc.resilient.backend.service.data.repositories.MovieRelationRepository;
import clc.resilient.backend.service.list.dtos.MovieListDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-05
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ListTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MovieListRepository movieListRepository;

    @Autowired
    private MovieRelationRepository movieRelationRepository;

    @Autowired
    private ObjectMapper mapper;

    @AfterEach
    public void clear() {
        movieRelationRepository.deleteAll();
        movieListRepository.deleteAll();
    }

    @Test
    void add_list() {
        var requestUrl = "/tmdb/4/list";
        var createRequest = MovieListDTO.builder()
            .description("Hey!")
            .name("My Cool List")
            .isPrivate(false)
            .build();

        var response = restTemplate.postForObject(requestUrl, createRequest, ResponseMessage.class);

        assertEquals(response.getSuccess(), true);
        var createdList = extractResult(response, MovieListDTO.class);
        assertEquals(createRequest.getDescription(), createdList.getDescription());
        assertEquals(createRequest.getName(), createdList.getName());
        assertEquals(createRequest.isPrivate(), createdList.isPrivate());
    }

    private <T> T extractResult(ResponseMessage response, Class<T> clazz) {
        return mapper.convertValue(response.getResults().get(0), clazz);
    }
}
