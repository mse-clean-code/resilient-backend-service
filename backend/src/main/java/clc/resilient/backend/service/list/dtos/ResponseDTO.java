package clc.resilient.backend.service.list.dtos;

import clc.resilient.backend.service.list.dtos.MovieListDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO {
    Boolean success;

    @JsonProperty("status_code")
    Integer statusCode;

    @JsonProperty("status_message")
    String statusMessage;

    Long id;

    // List<Object> results;

    @JsonUnwrapped
    @JsonInclude(JsonInclude.Include.NON_NULL)
    MovieListDTO movieListDTO;
}
