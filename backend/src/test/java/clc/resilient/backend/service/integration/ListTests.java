package clc.resilient.backend.service.integration;

import clc.resilient.backend.service.controllers.messages.ResponseMessage;
import clc.resilient.backend.service.data.repositories.MovieListRepository;
import clc.resilient.backend.service.data.repositories.MovieRelationRepository;
import clc.resilient.backend.service.list.dtos.MediaItemDTO;
import clc.resilient.backend.service.list.dtos.MediaItemsDTO;
import clc.resilient.backend.service.list.dtos.MovieListDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    void create_list() {
        var requestUrl = "/tmdb/4/list";
        var createRequest = MovieListDTO.builder()
            .description("Hey!")
            .name("My Cool List")
            .visible(false)
            .build();

        var response = restTemplate
            .postForObject(requestUrl, createRequest, ResponseMessage.class);

        assertEquals(response.getSuccess(), true);

        var createdList = response.getMovieListDTO();
        assertEquals(createRequest.getDescription(), createdList.getDescription());
        assertEquals(createRequest.getName(), createdList.getName());
        assertEquals(createRequest.isVisible(), createdList.isVisible());
        assertEquals(1, movieListRepository.count());
    }

    @Test
    void update_list() {
        var listId = addList();
        var requestUrl = "/tmdb/4/list/" + listId;
        var updateRequest = MovieListDTO.builder()
            .id(listId)
            .description("Hey Hey!")
            .name("My Cooler List")
            .visible(true)
            .build();

        var response = restTemplate
            .exchange(requestUrl, HttpMethod.PUT, new HttpEntity<>(updateRequest), ResponseMessage.class)
            .getBody();

        assertNotNull(response);
        assertTrue(response.getSuccess());
        var updatedList = response.getMovieListDTO();
        assertEquals(updateRequest.getDescription(), updatedList.getDescription());
        assertEquals(updateRequest.getName(), updatedList.getName());
        assertEquals(updateRequest.isVisible(), updatedList.isVisible());
        assertEquals(1, movieListRepository.count());
    }

    @Test
    void delete_list() {
        var listId = addList();
        var requestUrl = "/tmdb/4/" + listId;

        var response = restTemplate
            .exchange(requestUrl, HttpMethod.DELETE, null, ResponseMessage.class)
            .getBody();

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals(0, movieListRepository.count());
    }

    @Test
    void add_items() {
        var listId = addList();
        var requestUrl = "/tmdb/4/list/" + listId + "/items";
        var addRequest = new MediaItemsDTO();
        addRequest.setItems(List.of(
            MediaItemDTO.builder().mediaId(550L).mediaType("movie").build(),
            MediaItemDTO.builder().mediaId(244786L).mediaType("movie").build(),
            MediaItemDTO.builder().mediaId(1396L).mediaType("tv").build()
        ));

        var response = restTemplate
            .postForObject(requestUrl, addRequest, ResponseMessage.class);

        assertTrue(response.getSuccess());
        var list = response.getMovieListDTO();
        var items = list.getItems();
        assertEquals(3, items.size());
        assertTrue(() -> items.stream().anyMatch(item -> item.getMediaId().equals(550L)));
        assertTrue(() -> items.stream().anyMatch(item -> item.getMediaId().equals(244786L)));
        assertTrue(() -> items.stream().anyMatch(item -> item.getMediaId().equals(1396L)));
    }

    private Long addList() {
        var requestUrl = "/tmdb/4/list";
        var createRequest = MovieListDTO.builder()
            .description("Hey!")
            .name("My Cool List")
            .visible(false)
            .build();
        var response = restTemplate
            .postForObject(requestUrl, createRequest, ResponseMessage.class);
        return response.getId();
    }

    private <T> T extractResult(ResponseMessage response, Class<T> clazz) {
        return mapper.convertValue(response.getResults().get(0), clazz);
    }
}
