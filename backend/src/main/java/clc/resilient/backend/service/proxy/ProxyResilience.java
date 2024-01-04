package clc.resilient.backend.service.proxy;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-04
 */
public class ProxyResilience {
    // Names must match the config in `application.properties`
    // For example, PROXY RETRY with "proxyRetryApi" references
    // 'resilience4j.retry.instances.proxyRetryApi' in the config!
    public static final String PROXY_RETRY = "proxyRetryApi";
    public static final String PROXY_CIRCUIT_BREAKER = "proxyCircuitBreaker";
    public static final String PROXY_RATE_LIMITER = "proxyRateLimiterApi";
}
