package clc.resilient.backend.service.list.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-06
 */
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class MediaItemDTO {
    @JsonProperty("media_id")
    private String mediaId;
    @JsonProperty("media_type")
    private String mediaType;
    private Boolean success;
}
