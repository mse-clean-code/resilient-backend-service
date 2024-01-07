package clc.resilient.backend.service.list;

import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-06
 */
@ControllerAdvice(assignableTypes = {ListController.class})
public class ListExceptionHandler {

    // TODO: Exception Handling

    // Catch exceptions like EntityNotFoundException
    // Decide if to catch validation exception or not
    // Look for best practices!

    // See: https://www.baeldung.com/exception-handling-for-rest-with-spring#controlleradvice

    // Should resemble something like this

    // @ExceptionHandler({ SomeException.class })
    // @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    // public void handleSomeException(Exception ex) {
    //     logger.warn("woops something went through", ex);
    // }
}
