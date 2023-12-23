package clc.resilient.backend.service.proxy;

/**
 * @author Kacper Urbaniec
 * @version 2023-12-23
 */
public class TmdbOfflineException extends RuntimeException  {
    TmdbOfflineException(Throwable cause) {
        super("tmdb is offline", cause);
    }

    TmdbOfflineException() {
        this(null);
    }
}
