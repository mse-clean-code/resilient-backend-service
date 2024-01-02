package clc.resilient.backend.service.controllers.messages;

import clc.resilient.backend.service.data.objects.MovieList;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ResponseOfMovieListsPaginated {
    public ResponseOfMovieListsPaginated(Integer page, Integer totalPages, Integer totalResults, List<MovieList> results) {
        this.page = page;
        this.totalPages = totalPages;
        this.totalResults = totalResults;
        this.results = results;
    }

    Integer page;
    @JsonProperty("total_pages")
    Integer totalPages;
    @JsonProperty("total_results")
    Integer totalResults;
    List<MovieList> results;
}
