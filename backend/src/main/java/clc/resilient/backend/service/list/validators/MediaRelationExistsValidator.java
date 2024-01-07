package clc.resilient.backend.service.list.validators;

import clc.resilient.backend.service.common.exceptions.ApiException;
import clc.resilient.backend.service.list.entities.MediaRelation;
import clc.resilient.backend.service.list.repositories.MediaDataRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-06
 */

public class MediaRelationExistsValidator implements ConstraintValidator<MediaRelationExists, MediaRelation> {

    private final MediaDataRepository mediaDataRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MediaRelationExistsValidator(MediaDataRepository mediaDataRepository) {
        this.mediaDataRepository = mediaDataRepository;
    }

    @Override
    public boolean isValid(MediaRelation value, ConstraintValidatorContext context) {
        logger.debug("isValid({})", value);

        var id = value.getMediaId();
        var mediaType = value.getMediaType();

        try {
            var movieFound = mediaDataRepository.existsByIdAndMediaType(id, mediaType);
            logger.debug("Media exists: {} {}? {}", id, mediaType, movieFound);
            return movieFound;
        } catch (ApiException ex) {
            logger.warn("Could not fetch api data for {} {}", id, mediaType);
            logger.warn("isValid", ex);
        } catch (Exception ex) {
            logger.warn("Unknown error");
            logger.warn("isValid", ex);
        }
        return false;
    }
}