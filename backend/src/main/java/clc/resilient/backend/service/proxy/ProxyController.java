package clc.resilient.backend.service.proxy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Kacper Urbaniec
 * @version 2023-12-22
 */
@RestController
public class ProxyController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/test")
    public String test() {
        logger.debug("/test");
        return "test";
    }

    // Catches all tmdb operations besides lists
    @RequestMapping({"/tmdb/3/**", "/tmdb/4/auth/**", "/tmdb/4/account/**"})
    public ResponseEntity<String> mirrorRest(@RequestBody(required = false) String body,
                                     HttpMethod method, HttpServletRequest request, HttpServletResponse response)
        throws URISyntaxException {

        logger.debug("mirror | {} | {}", method.name(), request.getRequestURI());

        // TODO: Create tmdb client

        // Based on https://stackoverflow.com/a/49429650/12347616
        String requestUrl = request
            .getRequestURI()
            .replaceAll("^/tmdb", "");

        URI uri = new URI("https", null, "api.themoviedb.org", 443, null, null, null);
        uri = UriComponentsBuilder.fromUri(uri)
            .path(requestUrl)
            .query(request.getQueryString())
            .build(true).toUri();

        // Set all headers besides encoding (explained later)
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (Objects.equals(headerName, "Accept-Encoding")) continue;
            headers.set(headerName, request.getHeader(headerName));
        }

        // Fix encoding
        // Lead to 'Undocumented Error:OK' on Swagger
        // Debugger showed (in Chromium only) net::ERR_INCOMPLETE_CHUNKED_ENCODING 200 (OK)
        // Fix: https://stackoverflow.com/questions/54852953/postman-shows-could-not-get-any-response-even-though-response-is-ok
        headers.set("Accept-Encoding", "*/*");

        // API key
        // TODO: Move to config
        headers.set("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzY2ZjNmUxZGQyMzFiZDFmMmNhYTE5OGU3MzE3YTZhNCIsInN1YiI6IjYwZWZiOTZlYTQ0ZDA5MDAyZDQ0ZjNlNSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.nvvleDHS5FWTK9UbhKfeuW8L5w4hyjGHAphNtQJuYSY");

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        // TODO Add restTemplate bean in order to mock
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.exchange(uri, method, httpEntity, String.class);
        } catch(HttpStatusCodeException e) {
            // TODO: return custom exception on ResourceAccessException
            // https://stackoverflow.com/a/52271541/12347616
            return ResponseEntity.status(e.getStatusCode())
                .headers(e.getResponseHeaders())
                .body(e.getResponseBodyAsString());
        }
    }

    // TODO: Move to own feature
    // Catches all list CRUD operations
    @RequestMapping({"/tmdb/4/list/**", "/tmdb/4/{_}"})
    @SuppressWarnings("MVCPathVariableInspection")
    public ResponseEntity<String> listActions(@RequestBody(required = false) String body,
                                     HttpMethod method, HttpServletRequest request, HttpServletResponse response)
        throws URISyntaxException {

        logger.debug("Custom List Action | {} | {}", method.name(), request.getRequestURI());

        return mirrorRest(body, method, request, response);
    }

    @GetMapping(value = "/image.tmdb/**", produces = "application/octet-stream")
    public void image(@RequestBody(required = false) String body,
                                              HttpMethod method, HttpServletRequest request, HttpServletResponse response)
        throws URISyntaxException, IOException {

        logger.debug("Image | {} | {}", method.name(), request.getRequestURI());

        // TODO: Create tmdb client

        // Based on https://stackoverflow.com/a/49429650/12347616
        String requestUrl = request
            .getRequestURI()
            .replaceAll("^/image.tmdb", "");

        URI uri = new URI("https", null, "image.tmdb.org", 443, null, null, null);
        uri = UriComponentsBuilder.fromUri(uri)
            .path(requestUrl)
            .query(request.getQueryString())
            .build(true).toUri();

        // Set all headers besides encoding (explained later)
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (Objects.equals(headerName, "Accept-Encoding")) continue;
            headers.set(headerName, request.getHeader(headerName));
        }

        // Fix encoding
        // Lead to 'Undocumented Error:OK' on Swagger
        // Debugger showed (in Chromium only) net::ERR_INCOMPLETE_CHUNKED_ENCODING 200 (OK)
        // Fix: https://stackoverflow.com/questions/54852953/postman-shows-could-not-get-any-response-even-though-response-is-ok
        headers.set("Accept-Encoding", "*/*");

        // API key
        // TODO: Move to config
        headers.set("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzY2ZjNmUxZGQyMzFiZDFmMmNhYTE5OGU3MzE3YTZhNCIsInN1YiI6IjYwZWZiOTZlYTQ0ZDA5MDAyZDQ0ZjNlNSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.nvvleDHS5FWTK9UbhKfeuW8L5w4hyjGHAphNtQJuYSY");

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        // TODO Add restTemplate bean in order to mock
        RestTemplate restTemplate = new RestTemplate();
        try {
            // Based on https://stackoverflow.com/a/62528659/12347616
            restTemplate.execute(uri, method, null, clientHttpResponse -> {
                StreamUtils.copy(clientHttpResponse.getBody(), response.getOutputStream());
                return null;
            });
        } catch(HttpStatusCodeException e) {
            populateResponse(
                ResponseEntity.status(e.getStatusCode())
                     .headers(e.getResponseHeaders())
                     .body(e.getResponseBodyAsString()),
                response
            );
        }
    }

    // https://stackoverflow.com/a/51655139/12347616
    public static void populateResponse(ResponseEntity<String> responseEntity, HttpServletResponse servletResponse)
        throws IOException {
        for (Map.Entry<String, List<String>> header : responseEntity.getHeaders().entrySet()) {
            String chave = header.getKey();
            for (String valor : header.getValue()) {
                servletResponse.addHeader(chave, valor);
            }
        }

        servletResponse.setStatus(responseEntity.getStatusCode().value());
        if (responseEntity.hasBody()) {
            servletResponse.getWriter().write(responseEntity.getBody());
        }

    }
}
