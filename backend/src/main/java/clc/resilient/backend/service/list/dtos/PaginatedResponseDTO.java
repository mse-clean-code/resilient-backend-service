package clc.resilient.backend.service.list.dtos;

import clc.resilient.backend.service.list.dtos.MovieListDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponseDTO {
    Integer page;
    @JsonProperty("total_pages")
    Integer totalPages;
    @JsonProperty("total_results")
    Integer totalResults;
    List<MovieListDTO> results;
}
