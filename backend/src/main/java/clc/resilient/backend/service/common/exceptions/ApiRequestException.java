package clc.resilient.backend.service.common.exceptions;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-04
 */
public class ApiRequestException extends RuntimeException {
    public ApiRequestException(Throwable cause) {
        super("api request failed", cause);
    }
}
