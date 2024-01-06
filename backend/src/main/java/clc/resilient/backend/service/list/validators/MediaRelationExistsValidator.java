package clc.resilient.backend.service.list.validators;

import clc.resilient.backend.service.common.TmdbClient;
import clc.resilient.backend.service.list.entities.MediaRelation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Kacper Urbaniec
 * @version 2024-01-06
 */

public class MediaRelationExistsValidator implements ConstraintValidator<MediaRelationExists, MediaRelation> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TmdbClient client;

    public MediaRelationExistsValidator(TmdbClient client) {
        this.client = client;
    }

    @Override
    public boolean isValid(MediaRelation value, ConstraintValidatorContext context) {
        logger.debug("isValid({})", value);

        // TODO: Validate if added items if really exist
        // Could mimic "fetchTmdbItem" from "DefaultMovieListService"
        // Also add test case to "ListValidatorTest"!

        // Exception handling in constraint
        // https://www.baeldung.com/spring-mvc-custom-validator#2-creating-the-validator

        return true;
    }
}