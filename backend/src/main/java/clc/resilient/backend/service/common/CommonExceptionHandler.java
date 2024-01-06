package clc.resilient.backend.service.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-06
 */
@ControllerAdvice
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // TODO: Exception Handling

    // Catch exceptions like ApiOfflineException, ApiRequestException
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
