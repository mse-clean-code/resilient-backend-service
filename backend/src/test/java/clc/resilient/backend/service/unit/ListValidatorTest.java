package clc.resilient.backend.service.unit;

import clc.resilient.backend.service.list.entities.MediaRelation;
import clc.resilient.backend.service.list.entities.MovieList;
import clc.resilient.backend.service.list.services.DefaultMovieListService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-06
 */
@SpringBootTest
public class ListValidatorTest {

    @Autowired
    private DefaultMovieListService service;

    @Test
    void create_list_fail_name_null() {
        var list = new MovieList();
        list.setName(null);

        assertThrows(ConstraintViolationException.class, () -> {
            service.createList(list);
        });

        // TODO: Check exception message?
    }

    @Test
    void create_list_fail_name_empty() {
        var list = new MovieList();
        list.setName("");

        assertThrows(ConstraintViolationException.class, () -> {
            service.createList(list);
        });
    }

    @Test
    void create_list_fail_id_set() {
        var list = new MovieList();
        list.setId(42L);

        assertThrows(ConstraintViolationException.class, () -> {
            service.createList(list);
        });
    }

    @Test
    void update_list_fail_id_null() {
        var list = new MovieList();
        list.setId(null);

        assertThrows(ConstraintViolationException.class, () -> {
            service.updateList(list);
        });
    }

    @Test
    void create_list_with_movie_id_star_wars() {
        assertDoesNotThrow(() -> {
            var list = new MovieList();
            list.setName("List with star wars");
            list = service.createList(list);

            Set<MediaRelation> movies = new HashSet<>();
            var nonExistentMediaRelation = new MediaRelation();
            nonExistentMediaRelation.setMovieList(list);
            nonExistentMediaRelation.setMediaType("movie");
            nonExistentMediaRelation.setMediaId(11L);
            movies.add(nonExistentMediaRelation);
            service.addItemsToList(list.getId(), movies);
        });
    }

    @Test
    void update_list_fail_movie_id_doesnt_exists() {
        assertThrows(ConstraintViolationException.class, () -> {
            var list = new MovieList();
            list.setName("List with movies");
            list = service.createList(list);

            Set<MediaRelation> movies = new HashSet<>();
            var nonExistentMediaRelation = new MediaRelation();
            nonExistentMediaRelation.setMovieList(list);
            nonExistentMediaRelation.setMediaType("movie");
            nonExistentMediaRelation.setMediaId(-1L);
            movies.add(nonExistentMediaRelation);
            service.addItemsToList(list.getId(), movies);
        });
    }

    @Test
    void update_list_fail_movie_id_might_not_exists() {
        assertThrows(ConstraintViolationException.class, () -> {
            var list = new MovieList();
            list.setName("List with movies");
            list = service.createList(list);

            Set<MediaRelation> movies = new HashSet<>();
            var nonExistentMediaRelation = new MediaRelation();
            nonExistentMediaRelation.setMovieList(list);
            nonExistentMediaRelation.setMediaType("movie");
            nonExistentMediaRelation.setMediaId(Long.MAX_VALUE);
            movies.add(nonExistentMediaRelation);
            service.addItemsToList(list.getId(), movies);
        });
    }
}