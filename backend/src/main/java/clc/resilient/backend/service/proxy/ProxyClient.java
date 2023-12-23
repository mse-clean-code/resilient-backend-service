package clc.resilient.backend.service.proxy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * @author Kacper Urbaniec
 * @version 2023-12-23
 */
@Component
public class ProxyClient {
    private final URI baseUri;
    private final String apiReadAccessKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    ProxyClient(
        @Value("${tmdb.api.uri}") String uri,
        @Value("${tmdb.api.read-access-key}") String apiReadAccessKey
    ) throws URISyntaxException {
        this.baseUri = new URI(uri);
        this.apiReadAccessKey = apiReadAccessKey;
    }

    // https://developer.themoviedb.org/reference/intro/getting-started
    public ResponseEntity<String> fetchTmdbApi(
        @NotNull HttpMethod method, @NotNull String path,
        @NotNull HttpServletRequest request, String body
    ) {
        // Proxy fetch based on https://stackoverflow.com/a/49429650/12347616
        logger.debug("fetchTmdbApi({}, {}, {}, {})", method, path, request.getQueryString(), body);
        var uri = buildPathURI(path, request);
        var headers = buildHeaders(request);
        var httpEntity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(uri, method, httpEntity, String.class);
        } catch (ResourceAccessException ex) {
            // Throw custom exception when external API is offline
            // https://stackoverflow.com/a/52271541/12347616
            logger.error("tmdb is offline");
            throw new TmdbOfflineException(ex);
        }
    }

    private URI buildPathURI(String path, HttpServletRequest request) {
        return UriComponentsBuilder.fromUri(baseUri)
            .path(path)
            .query(request.getQueryString())
            .build(true).toUri();
    }

    private HttpHeaders buildHeaders(HttpServletRequest request) {
        // Set all headers besides encoding (explained later)
        var headers = new HttpHeaders();
        var headerNames = request.getHeaderNames();
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

        // Set API key
        headers.set("Authorization", "Bearer " + apiReadAccessKey);
        return headers;
    }


    // https://developer.themoviedb.org/docs/image-basics

}
