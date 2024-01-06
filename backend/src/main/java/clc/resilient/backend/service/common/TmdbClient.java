package clc.resilient.backend.service.common;

import clc.resilient.backend.service.common.exceptions.ApiOfflineException;
import clc.resilient.backend.service.common.exceptions.ApiRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
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
public class TmdbClient {
    private final URI apiUri;
    private final String apiReadAccessKey;
    private final URI imageUri;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TmdbClient(
        @Value("${tmdb.api.uri}") String apiUri,
        @Value("${tmdb.api.read-access-key}") String apiReadAccessKey,
        @Value("${tmdb.image.uri}") String imageUri
    ) throws URISyntaxException {
        this.apiUri = new URI(apiUri);
        this.apiReadAccessKey = apiReadAccessKey;
        this.imageUri = new URI(imageUri);
    }

    /**
     * Method for fetching resources from tmdb api.
     * See <a href="https://developer.themoviedb.org/reference/intro/getting-started">docs</a>.
     */
    public ResponseEntity<String> fetchTmdbApi(
        @NotNull HttpMethod method, @NotNull String path,
        @NotNull HttpServletRequest request, String body
    ) {
        // Proxy fetch based on https://stackoverflow.com/a/49429650/12347616
        logger.debug("fetchTmdbApi({}, {}, {}, {})", method, path, request.getQueryString(), body);
        var uri = buildPathURI(apiUri, path, request);
        var headers = buildHeaders(request);
        var httpEntity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(uri, method, httpEntity, String.class);
        } catch (ResourceAccessException ex) {
            // Throw custom exception when external API is offline
            // https://stackoverflow.com/a/52271541/12347616
            logger.error("tmdb is offline");
            throw new ApiOfflineException(ex);
        } catch (RestClientException ex) {
            logger.error("tmdb request failed");
            throw new ApiRequestException(ex);
        } catch (Exception ex) {
            logger.error("unknown tmdb access exception");
            throw new IllegalStateException("unknown exception", ex);
        }
    }

    /**
     * Method for fetching image resources from tmdb image api.
     * See <a href="https://developer.themoviedb.org/docs/image-basics">docs</a>.
     */
    public void fetchTmdbImage(
        @NotNull HttpMethod method, @NotNull String path,
        @NotNull HttpServletRequest request, String body,
        @NotNull HttpServletResponse response
    ) {
        // File fetch based on https://stackoverflow.com/a/62528659/12347616
        logger.debug("fetchTmdbImage({}, {}, {}, {}, {})", method, path, request.getQueryString(), body, response);
        var uri = buildPathURI(imageUri, path, request);

        try {
            restTemplate.execute(uri, method, null, clientHttpResponse -> {
                StreamUtils.copy(clientHttpResponse.getBody(), response.getOutputStream());
                return null;
            });
        } catch (ResourceAccessException ex) {
            // Throw custom exception when external API is offline
            // https://stackoverflow.com/a/52271541/12347616
            logger.error("tmdb is offline");
            throw new ApiOfflineException(ex);
        } catch (RestClientException ex) {
            logger.error("tmdb request failed");
            throw new ApiRequestException(ex);
        } catch (Exception ex) {
            logger.error("unknown tmdb access exception");
            throw new IllegalStateException("unknown exception", ex);
        }
    }

    private URI buildPathURI(URI base, String path, HttpServletRequest request) {
        return UriComponentsBuilder.fromUri(base)
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
}
