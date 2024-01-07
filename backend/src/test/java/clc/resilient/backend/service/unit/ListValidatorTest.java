package clc.resilient.backend.service.unit;

import clc.resilient.backend.service.list.entities.MovieList;
import clc.resilient.backend.service.list.services.DefaultMovieListService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // TODO: MediaRelationExists & other Test!
}
