package clc.resilient.backend.service.common.exceptions;

/**
 * @author Kacper Urbaniec
 * @version 2023-12-23
 */
public class ApiOfflineException extends RuntimeException {

    public ApiOfflineException(Throwable cause) {
        super("api is offline", cause);
    }
}
