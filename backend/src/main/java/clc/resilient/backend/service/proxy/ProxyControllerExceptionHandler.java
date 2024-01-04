package clc.resilient.backend.service.proxy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-04
 */
@ControllerAdvice(assignableTypes = {ProxyController.class})
public class ProxyControllerExceptionHandler {
    // Exception handling for circuit breaker
    @ExceptionHandler({ CallNotPermittedException.class })
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public void handleCallNotPermittedException() {}
}
