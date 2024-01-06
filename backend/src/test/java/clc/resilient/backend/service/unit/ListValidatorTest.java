package clc.resilient.backend.service.unit;

import clc.resilient.backend.service.data.objects.MovieList;
import clc.resilient.backend.service.data.services.MovieListQueryService;
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
    private MovieListQueryService service;

    @Test
    void add_list_fail_name_null() {
        var list = new MovieList();
        list.setName(null);

        assertThrows(ConstraintViolationException.class, () -> {
            service.addList(list);
        });
    }

    @Test
    void add_list_fail_name_empty() {
        var list = new MovieList();
        list.setName("");

        assertThrows(ConstraintViolationException.class, () -> {
            service.addList(list);
        });
    }
}
