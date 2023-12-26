
package clc.resilient.backend.service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Kacper Urbaniec
 * @version 2023-12-23
 */
@Configuration
public class WebMvcCorsConfiguration {
    @Bean
    public WebMvcConfigurer corsConfiguration() {
        return new WebMvcConfigurer() {
            @Override
            @SuppressWarnings("NullableProblems")
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins(CorsConfiguration.ALL)
                    .allowedHeaders(CorsConfiguration.ALL)
                    .allowedMethods(CorsConfiguration.ALL);
            }
        };
    }
}
