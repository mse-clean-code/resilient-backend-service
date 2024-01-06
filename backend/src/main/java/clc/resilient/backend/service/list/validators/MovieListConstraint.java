package clc.resilient.backend.service.list.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-06
 */
@Documented
@Constraint(validatedBy = MovieListValidator.class)
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MovieListConstraint {
    String message() default "Invalid movie listt";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
