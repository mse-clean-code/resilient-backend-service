package clc.resilient.backend.service.proxy;

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

/**
 * @author Kacper Urbaniec
 * @version 2023-12-22
 */
@RestController
public class ProxyController {
    private final ProxyClient client;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ProxyController(@Autowired ProxyClient client) {
        this.client = client;
    }

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

    /**
     * Function that is executed when circuit breaker is open.
     */
    @SuppressWarnings("unused")
    public ResponseEntity<String> circuitBreakerFallback(CallNotPermittedException ex) {
        // Note: Specific exception type is important! Else retry fallback will be always executed
        // https://resilience4j.readme.io/docs/getting-started-3#fallback-methods
        logger.debug("circuitBreakerFallback({})", ex.getMessage());
        return new ResponseEntity<>("service is unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Function that is executed when circuit breaker is open.
     */
    @SuppressWarnings("unused")
    public ResponseEntity<String> rateLimiterFallback(RequestNotPermitted ex) {
        logger.debug("rateLimiterFallback({})", ex.getMessage());
        return new ResponseEntity<>("too many requests", HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Function that is executed when all retries attempts have exhausted.
     */
    @SuppressWarnings("unused")
    public ResponseEntity<String> retryFallback(Exception ex) {
        logger.debug("retryFallback({})", ex.getMessage());
        return new ResponseEntity<>("all retries have exhausted", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
