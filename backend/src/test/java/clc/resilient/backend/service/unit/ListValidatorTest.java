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

    // Even though these are unit tests we need spring
    // as the validator is managed by the framework.
    // Controllers/Services are decorated with corresponding validation calls
    // that also consider validation groups.

    @Test
    void create_list_fail_name_null() {
        var list = new MovieList();
        list.setName(null);

        var ex = assertThrows(ConstraintViolationException.class, () -> {
            service.createList(list);
        });

        assertTrue(() -> ex.getMessage().contains("createList.addList.name"));
    }

    @Test
    void create_list_fail_name_empty() {
        var list = new MovieList();
        list.setName("");

        var ex = assertThrows(ConstraintViolationException.class, () -> {
            service.createList(list);
        });

        assertTrue(() -> ex.getMessage().contains("createList.addList.name"));
    }

    @Test
    void create_list_fail_id_set() {
        var list = new MovieList();
        list.setId(42L);

        var ex = assertThrows(ConstraintViolationException.class, () -> {
            service.createList(list);
        });

        assertTrue(() -> ex.getMessage().contains("createList.addList.name"));
    }

    @Test
    void update_list_fail_id_null() {
        var list = new MovieList();
        list.setId(null);

        var ex = assertThrows(ConstraintViolationException.class, () -> {
            service.updateList(list);
        });

        assertTrue(() -> ex.getMessage().contains("updateList.updateList.id"));
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
        var ex = assertThrows(ConstraintViolationException.class, () -> {
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

        assertTrue(() -> ex.getMessage().contains("No such movie/tv"));
    }

    @Test
    void update_list_fail_movie_id_might_not_exists() {
        var ex = assertThrows(ConstraintViolationException.class, () -> {
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

        assertTrue(() -> ex.getMessage().contains("No such movie/tv"));
    }
}