package clc.resilient.backend.service.list.dtos;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-06
 */
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class MediaItemDTO {
    @JsonProperty("media_id")
    private Long mediaId;
    @JsonProperty("media_type")
    private String mediaType;
    private Boolean success;

    @JsonIgnore
    private Map<String, Object> apiData;

    // @JsonUnwrapped does not work on maps
    // https://github.com/FasterXML/jackson-databind/issues/171
    @JsonAnyGetter
    public Map<String, Object> getMap() {
        return apiData;
    }

    @JsonAnySetter
    public void add(String key, Object value) {
        // Occurs only during testing
        if (apiData == null) apiData = new HashMap<>();
        apiData.put(key, value);
    }
}
