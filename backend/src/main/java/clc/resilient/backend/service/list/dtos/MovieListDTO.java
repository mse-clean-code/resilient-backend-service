package clc.resilient.backend.service.list.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Kacper Urbaniec
 * @version 2024-01-05
 */
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class MovieListDTO {
    private Long id;
    private String name;
    private String description;
    @JsonProperty("iso_639_1")
    private String iso6391;
    @JsonProperty("public")
    private boolean visible;
    private List<MediaItemDTO> results;
    @JsonProperty("number_of_items")
    private int numberOfItems;
    @JsonProperty("backdrop_path")
    private String backdropPath;
}
