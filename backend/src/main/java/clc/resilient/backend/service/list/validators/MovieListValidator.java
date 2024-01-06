package clc.resilient.backend.service.list.validators;

import clc.resilient.backend.service.data.objects.MovieList;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


// Note: Does not seem to work...

// /**
//  * @author Kacper Urbaniec
//  * @version 2024-01-06
//  */
// // @Component
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
