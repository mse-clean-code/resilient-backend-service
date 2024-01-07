package clc.resilient.backend.service.list.validators;

import clc.resilient.backend.service.common.SimpleHttpServletRequest;
import clc.resilient.backend.service.common.TmdbClient;
import clc.resilient.backend.service.common.exceptions.ApiRequestException;
import clc.resilient.backend.service.list.entities.MediaRelation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import java.util.Map;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-06
 */

public class MediaRelationExistsValidator implements ConstraintValidator<MediaRelationExists, MediaRelation> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TmdbClient tmdbClient;
    private final ObjectMapper objectMapper;

    public MediaRelationExistsValidator(TmdbClient client, ObjectMapper objectMapper) {
        this.tmdbClient = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isValid(MediaRelation value, ConstraintValidatorContext context) {
        logger.debug("isValid({})", value);

        var id = value.getMediaId();
        var mediaType = value.getMediaType();
        String requestUrl = String.format("/3/movie/%s", id);
        try {

            var response = tmdbClient
                    .fetchTmdbApi(HttpMethod.GET, requestUrl, new SimpleHttpServletRequest(), null);
            var json = response.getBody();

            try {
                @SuppressWarnings("unchecked")
                var data = (Map<String, Object>) objectMapper.readValue(json, Map.class);
                value.setApiData(data);
            } catch (JsonProcessingException | ClassCastException ex) {
                logger.warn("Could not fetch api data for {} {}", id, mediaType);
                logger.warn("fetchTmdbItem", ex);
            }

            return true;
        } catch(ApiRequestException e) {
            logger.warn(String.format("Movie was not found: %s", e.getCause().getMessage()));
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format("While fetching from api.themoviedb: %s", e));
            return false;
        }
    }
}