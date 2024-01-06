package clc.resilient.backend.service.proxy;

import clc.resilient.backend.service.common.TmdbClient;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

import java.io.IOException;

/**
 * @author Kacper Urbaniec
 * @version 2023-12-22
 */
@RestController
public class ProxyController {
    private final TmdbClient client;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ProxyController(@Autowired TmdbClient client) {
        this.client = client;
    }

    // TODO: Error handling for proxy exceptions!

    /**
     * Endpoint that proxies all tmdb methods besides list functionality.
     */
    @CircuitBreaker(name = ProxyResilience.PROXY_CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallback")
    @RateLimiter(name = ProxyResilience.PROXY_RATE_LIMITER, fallbackMethod = "rateLimiterFallback")
    @Retry(name = ProxyResilience.PROXY_RETRY, fallbackMethod = "retryFallback")
    @RequestMapping({"/tmdb/3/**", "/tmdb/4/auth/**", "/tmdb/4/account/**"})
    public ResponseEntity<String> tmdbApi(
        HttpMethod method, HttpServletRequest request,
        @RequestBody(required = false) String body, HttpServletResponse response
    ) {
        logger.debug("tmdbApi({}, {}, {})", method, request.getRequestURI(), body);
        // Create path without /tmdb prefix so that is it compatible
        // with the original `tmdb` api
        String path = request
            .getRequestURI()
            .replaceAll("^/tmdb", "");
        return client.fetchTmdbApi(method, path, request, body);
    }

    /**
     * Endpoint that proxies all tmdb image methods.
     */
    @CircuitBreaker(name = ProxyResilience.PROXY_CIRCUIT_BREAKER, fallbackMethod = "circuitBreakerFallback")
    @RateLimiter(name = ProxyResilience.PROXY_RATE_LIMITER, fallbackMethod = "rateLimiterFallback")
    @Retry(name = ProxyResilience.PROXY_RETRY, fallbackMethod = "retryFallback")
    @GetMapping(value = "/image.tmdb/**", produces = "application/octet-stream")
    public void tmdbImage(
        HttpMethod method, HttpServletRequest request,
        @RequestBody(required = false) String body, HttpServletResponse response
    ) {
        logger.debug("tmdbImage({}, {}, {})", method, request.getRequestURI(), body);
        // Create path without /image.tmdb prefix so that is it compatible
        // with the original `tmdb` image api
        String path = request
            .getRequestURI()
            .replaceAll("^/image.tmdb", "");
        client.fetchTmdbImage(method, path, request, body, response);
    }

    //================================================================================
    // Resilience4j Fallbacks
    //================================================================================

    //region tmdb api fallbacks

    /**
     * Function that is executed when circuit breaker is open.
     */
    @SuppressWarnings("unused")
    public ResponseEntity<String> circuitBreakerFallback(CallNotPermittedException ex) {
        // Note: Specific exception type is important! Else retry fallback will be always executed
        // https://resilience4j.readme.io/docs/getting-started-3#fallback-methods
        logger.warn("circuitBreakerFallback", ex);
        return new ResponseEntity<>("service is unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Function that is executed when rate limit has been exceeded.
     */
    @SuppressWarnings("unused")
    public ResponseEntity<String> rateLimiterFallback(RequestNotPermitted ex) {
        logger.warn("rateLimiterFallback", ex);
        return new ResponseEntity<>("too many requests", HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Function that is executed when all retries attempts have exhausted.
     */
    @SuppressWarnings("unused")
    public ResponseEntity<String> retryFallback(Exception ex) {
        logger.warn("retryFallback", ex);
        return new ResponseEntity<>("all retries have exhausted", HttpStatus.SERVICE_UNAVAILABLE);
    }

    //endregion

    //region tmdb image fallbacks

    /**
     * Function that is executed when circuit breaker is open.
     */
    @SuppressWarnings("unused")
    public void circuitBreakerFallback(
        HttpMethod method, HttpServletRequest request,
        @RequestBody(required = false) String body, HttpServletResponse response,
        CallNotPermittedException ex
    ) throws IOException {
        logger.warn("circuitBreakerFallback", ex);
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.getWriter().write("service is unavailable");
    }

    /**
     * Function that is executed when rate limit has been exceeded.
     */
    @SuppressWarnings("unused")
    public void rateLimiterFallback(
        HttpMethod method, HttpServletRequest request,
        @RequestBody(required = false) String body, HttpServletResponse response,
        RequestNotPermitted ex
    ) throws IOException {
        logger.warn("rateLimiterFallback", ex);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write("too many requests");
    }

    /**
     * Function that is executed when all retries attempts have exhausted.
     */
    @SuppressWarnings("unused")
    public void retryFallback(
        HttpMethod method, HttpServletRequest request,
        @RequestBody(required = false) String body, HttpServletResponse response,
        Exception ex
    ) throws IOException {
        logger.warn("retryFallback", ex);
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.getWriter().write("all retries have exhausted");
    }

    //endregion
}
