package clc.resilient.backend.service.common.exceptions;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-07
 */
public abstract class ApiException extends RuntimeException {

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
