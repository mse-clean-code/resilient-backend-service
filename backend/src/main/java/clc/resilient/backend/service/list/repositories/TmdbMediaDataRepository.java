package clc.resilient.backend.service.list.repositories;

import clc.resilient.backend.service.common.SimpleHttpServletRequest;
import clc.resilient.backend.service.common.TmdbClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-07
 */
@Component
@Validated
public class TmdbMediaDataRepository implements MediaDataRepository {

    private final TmdbClient tmdbClient;
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TmdbMediaDataRepository(TmdbClient tmdbClient, ObjectMapper objectMapper) {
        this.tmdbClient = tmdbClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> findByMediaIdAndMediaType(@NotNull Long id, @NotNull String mediaType) {
        logger.debug("findByMediaIdAndMediaType({}, {})", id, mediaType);
        var response = fetchTmdbItem(id, mediaType);
        String json;
        if (response == null || !response.hasBody()) json = "{}";
        else json = response.getBody();

        try {
            return (Map<String, Object>) objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException | ClassCastException ex) {
            logger.warn("Could not fetch media data for {} {}", id, mediaType);
            logger.warn("findByMediaIdAndMediaType", ex);
            return null;
        }
    }

    @Override
    public boolean existsByIdAndMediaType(@NotNull Long id, @NotNull String mediaType) {
        logger.debug("existsByIdAndMediaType({}, {})", id, mediaType);
        var response = fetchTmdbItem(id, mediaType);
        if (response == null) return false;
        return !response.getStatusCode().isError();
    }

    private ResponseEntity<String> fetchTmdbItem(@NotNull Long id, @NotNull String mediaType) {
        String requestUrl;
        if (mediaType.equals("movie")) requestUrl = "/3/movie/" + id;
        else requestUrl = "/3/tv/" + id;

        return tmdbClient
            .fetchTmdbApi(HttpMethod.GET, requestUrl, new SimpleHttpServletRequest(), null);
    }
}
