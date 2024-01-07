package clc.resilient.backend.service.list.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-06
 */
@Documented
@Constraint(validatedBy = MediaRelationExistsValidator.class)
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MediaRelationExists {
    String message() default "No such movie/tv";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
