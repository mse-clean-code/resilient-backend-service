package clc.resilient.backend.service.integration;

import clc.resilient.backend.service.list.dtos.ResponseDTO;
import clc.resilient.backend.service.list.dtos.PaginatedResponseDTO;
import clc.resilient.backend.service.list.repositories.MovieListRepository;
import clc.resilient.backend.service.list.repositories.MediaRelationRepository;
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
    private MediaRelationRepository movieRelationRepository;

    @Autowired
    private ObjectMapper mapper;

    @AfterEach
    public void clear() {
        movieRelationRepository.deleteAll();
        movieListRepository.deleteAll();
    }

    @Test
    void get_lists() {
        for (int i = 0; i < 10; ++i) {
            addList();
        }
        var requestUrl = "/tmdb/4/account/0/lists";

        var response = restTemplate
            .getForObject(requestUrl, PaginatedResponseDTO.class);

        assertEquals(1,  response.getPage());
        assertEquals(1,  response.getTotalPages());
        assertEquals(10, response.getTotalResults());
        assertEquals(10, response.getResults().size());
    }

    @Test
    void get_list() {
        var addListResult = addList();
        var listId = addListResult.id();
        var requestUrl = "/tmdb/4/list/" + listId;

        var list = restTemplate
            .getForObject(requestUrl, MovieListDTO.class);

        assertNotNull(list);
        assertNotNull(list.getName());
        assertNotNull(list.getDescription());
        assertFalse(list.getResults().isEmpty());
    }

    @Test
    void fail_get_list_that_does_not_exist() {
        var listId = 101L;
        var requestUrl = "/tmdb/4/list/" + listId;

        var response = restTemplate
            .getForEntity(requestUrl, String.class);

        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("List " + listId + " does not exist", response.getBody());
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
            .postForObject(requestUrl, createRequest, ResponseDTO.class);

        assertEquals(response.getSuccess(), true);

        var createdList = response.getMovieListDTO();
        assertEquals(createRequest.getDescription(), createdList.getDescription());
        assertEquals(createRequest.getName(), createdList.getName());
        assertEquals(createRequest.getVisible(), createdList.getVisible());
        assertEquals(1, movieListRepository.count());
    }

    @Test
    void fail_create_list_null_name() {
        var requestUrl = "/tmdb/4/list";
        var createRequest = MovieListDTO.builder()
            .description("Hey!")
            .name(null)
            .visible(false)
            .build();

        var response = restTemplate
            .postForEntity(requestUrl, createRequest, String.class);

        assertTrue(response.getStatusCode().is4xxClientError());
        assertTrue(() -> {
            var errorMsg = response.getBody();
            assertNotNull(errorMsg);
            return errorMsg.contains("createList.addList.name");
        });
    }

    @Test
    void update_list() {
        var addListResult = addList();
        var listId = addListResult.id();
        var requestUrl = "/tmdb/4/list/" + listId;
        var updateRequest = MovieListDTO.builder()
            .id(listId)
            .description("Hey Hey!")
            .name("My Cooler List")
            .visible(true)
            .build();

        var response = restTemplate
            .exchange(requestUrl, HttpMethod.PUT, new HttpEntity<>(updateRequest), ResponseDTO.class)
            .getBody();

        assertNotNull(response);
        assertTrue(response.getSuccess());
        var updatedList = response.getMovieListDTO();
        assertEquals(updateRequest.getDescription(), updatedList.getDescription());
        assertEquals(updateRequest.getName(), updatedList.getName());
        assertEquals(updateRequest.getVisible(), updatedList.getVisible());
        assertEquals(1, movieListRepository.count());
    }

    @Test
    void fail_update_list_that_does_not_exist() {
        var listId = 101L;
        var requestUrl = "/tmdb/4/list/" + listId;
        var updateRequest = MovieListDTO.builder()
            .id(listId)
            .description("Hey Hey!")
            .name("My Cooler List")
            .visible(true)
            .build();

        var response = restTemplate
            .exchange(requestUrl, HttpMethod.PUT, new HttpEntity<>(updateRequest), String.class);

        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("List " + listId + " does not exist", response.getBody());
    }

    @Test
    void delete_list() {
        var addListResult = addList();
        var listId = addListResult.id();
        var requestUrl = "/tmdb/4/" + listId;

        var response = restTemplate
            .exchange(requestUrl, HttpMethod.DELETE, null, ResponseDTO.class)
            .getBody();

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals(0, movieListRepository.count());
    }

    @Test
    void add_items() {
        var addListResult = addListWithoutItems();
        var listId = addListResult.id();
        var requestUrl = "/tmdb/4/list/" + listId + "/items";
        var addRequest = new MediaItemsDTO();
        addRequest.setItems(List.of(
            MediaItemDTO.builder().mediaId(550L).mediaType("movie").build(),
            MediaItemDTO.builder().mediaId(244786L).mediaType("movie").build(),
            MediaItemDTO.builder().mediaId(1396L).mediaType("tv").build()
        ));

        var response = restTemplate
            .postForObject(requestUrl, addRequest, ResponseDTO.class);

        assertTrue(response.getSuccess());
        var list = response.getMovieListDTO();
        var items = list.getResults();
        assertEquals(3, items.size());
        assertTrue(() -> items.stream().anyMatch(item -> item.getMediaId().equals(550L)));
        assertTrue(() -> items.stream().anyMatch(item -> item.getMediaId().equals(244786L)));
        assertTrue(() -> items.stream().anyMatch(item -> item.getMediaId().equals(1396L)));
    }

    @Test
    void fail_add_items_to_list_that_does_not_exist() {
        var listId = 101L;
        var requestUrl = "/tmdb/4/list/" + listId + "/items";
        var addRequest = new MediaItemsDTO();
        addRequest.setItems(List.of(
            MediaItemDTO.builder().mediaId(550L).mediaType("movie").build(),
            MediaItemDTO.builder().mediaId(244786L).mediaType("movie").build(),
            MediaItemDTO.builder().mediaId(1396L).mediaType("tv").build()
        ));

        var response = restTemplate
            .postForEntity(requestUrl, addRequest, String.class);

        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("List " + listId + " does not exist", response.getBody());
    }

    @Test
    void remove_items() {
        var addListResult = addList();
        var listId = addListResult.id();
        var requestUrl = "/tmdb/4/list/" + listId + "/items";
        var removeRequest = new MediaItemsDTO();
        removeRequest.setItems(List.of(
            MediaItemDTO.builder().mediaId(244786L).mediaType("movie").build(),
            MediaItemDTO.builder().mediaId(1396L).mediaType("tv").build()
        ));

        var response = restTemplate
            .exchange(requestUrl, HttpMethod.DELETE, new HttpEntity<>(removeRequest), ResponseDTO.class)
            .getBody();

        assertNotNull(response);
        assertTrue(response.getSuccess());
        var list = response.getMovieListDTO();
        var items = list.getResults();
        assertEquals(1, items.size());
        assertTrue(() -> items.stream().anyMatch(item -> item.getMediaId().equals(550L)));
    }

    public record AddListResult(Long id, MovieListDTO list) {}

    private AddListResult addListWithoutItems() {
        return addList(false);
    }

    private AddListResult addList() {
        return addList(true);
    }

    private AddListResult addList(boolean withItems) {
        var requestUrl = "/tmdb/4/list";
        var createRequest = MovieListDTO.builder()
            .description("Hey!")
            .name("My Cool List")
            .visible(false)
            .build();
        var response = restTemplate
            .postForObject(requestUrl, createRequest, ResponseDTO.class);
        var listId = response.getId();

        if (!withItems)
            return new AddListResult(listId, response.getMovieListDTO());

        requestUrl = "/tmdb/4/list/" + listId + "/items";
        var addRequest = new MediaItemsDTO();
        addRequest.setItems(List.of(
            MediaItemDTO.builder().mediaId(550L).mediaType("movie").build(),
            MediaItemDTO.builder().mediaId(244786L).mediaType("movie").build(),
            MediaItemDTO.builder().mediaId(1396L).mediaType("tv").build()
        ));

        response = restTemplate
            .postForObject(requestUrl, addRequest, ResponseDTO.class);

        return new AddListResult(listId, response.getMovieListDTO());
    }

}
