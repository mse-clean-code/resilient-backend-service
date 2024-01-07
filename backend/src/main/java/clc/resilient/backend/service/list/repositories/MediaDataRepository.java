package clc.resilient.backend.service.list.repositories;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-07
 */
public interface MediaDataRepository {
    Map<String, Object> findByMediaIdAndMediaType(@NotNull Long id, @NotNull String mediaType);

    boolean existsByIdAndMediaType(@NotNull Long id, @NotNull String mediaType);
}
