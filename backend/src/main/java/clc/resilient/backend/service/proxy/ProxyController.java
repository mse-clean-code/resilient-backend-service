package clc.resilient.backend.service.proxy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kacper Urbaniec
 * @version 2023-12-22
 */
@RestController
public class ProxyController {
    private final ProxyClient client;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    ProxyController(@Autowired ProxyClient client) {
        this.client = client;
    }

    @GetMapping("/test")
    public String test() {
        logger.debug("/test");
        return "test";
    }

    /**
     * Endpoint that proxies all tmdb methods besides list functionality.
     */
    @RequestMapping({"/tmdb/3/**", "/tmdb/4/auth/**", "/tmdb/4/account/**"})
    public ResponseEntity<String> tmdbApi(
        HttpMethod method, HttpServletRequest request,
        @RequestBody(required = false) String body, HttpServletResponse response
    ) {
        logger.debug("tmdbApi({}, {}, {})", method, request.getRequestURI(), body);
        // Create path without /tmdb prefix so that is it compatible
        // with the original `tmdb` api
        String path = request
            .getRequestURI()
            .replaceAll("^/tmdb", "");
        return client.fetchTmdbApi(method, path, request, body);
    }


    /**
     * Endpoint that proxies all tmdb image methods.
     */
    @GetMapping(value = "/image.tmdb/**", produces = "application/octet-stream")
    public void tmdbImage(
        HttpMethod method, HttpServletRequest request,
        @RequestBody(required = false) String body, HttpServletResponse response
    ) {
        logger.debug("tmdbImage({}, {}, {})", method, request.getRequestURI(), body);
        // Create path without /image.tmdb prefix so that is it compatible
        // with the original `tmdb` image api
        String path = request
            .getRequestURI()
            .replaceAll("^/image.tmdb", "");
        client.fetchTmdbImage(method, path, request, body, response);
    }

    // TODO: Move to own feature
    // Catches all list CRUD operations
    @RequestMapping({"/tmdb/4/list/**", "/tmdb/4/{_}"})
    @SuppressWarnings("MVCPathVariableInspection")
    public ResponseEntity<String> listActions(@RequestBody(required = false) String body,
                                              HttpMethod method, HttpServletRequest request, HttpServletResponse response) {

        logger.debug("Custom List Action | {} | {}", method.name(), request.getRequestURI());

        return tmdbApi(method, request, body, response);
    }
}
