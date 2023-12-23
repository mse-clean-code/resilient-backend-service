package clc.resilient.backend.service.list;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kacper Urbaniec
 * @version 2023-12-23
 */
@RestController
public class ListController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Will be resolved in issue #3
    // TODO: Split to multiple endpoints
    // TODO: Adapt `tmdb-v4.json` if endpoint signature differs from original tmdb api
    //   Needed for swagger / openapi compatibility

    // Catches all list CRUD operations
    @RequestMapping({"/tmdb/4/list/**", "/tmdb/4/{_}"})
    @SuppressWarnings("MVCPathVariableInspection")
    public ResponseEntity<String> listActions(
        @RequestBody(required = false) String body,
        HttpMethod method, HttpServletRequest request,
        HttpServletResponse response
    ) {
        logger.debug("Custom List Action | {} | {}", method.name(), request.getRequestURI());
        throw new NotImplementedException();
    }
}
