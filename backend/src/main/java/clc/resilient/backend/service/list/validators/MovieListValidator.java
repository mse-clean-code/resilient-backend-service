package clc.resilient.backend.service.list.validators;

import clc.resilient.backend.service.data.objects.MovieList;
import clc.resilient.backend.service.data.repositories.MovieListRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


/**
 * @author Kacper Urbaniec
 * @version 2024-01-06
 */
// @Component
// public class MovieListValidator implements Validator {
//     @Override
//     public boolean supports(Class<?> clazz) {
//         return MovieList.class.isAssignableFrom(clazz);
//     }
//
//     @Override
//     public void validate(Object target, Errors errors) {
//         var list = (MovieList) target;
//         if (errors.hasErrors()) return;
//
//         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
//     }
// }

public class MovieListValidator implements ConstraintValidator<MovieListConstraint, Object> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MovieListRepository repository;

    public MovieListValidator(MovieListRepository repository) {
        this.repository = repository;
    }

    @Override
    public void initialize(MovieListConstraint constraintAnnotation) { }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        logger.debug("isValid({})", value);

        return true;
    }
}