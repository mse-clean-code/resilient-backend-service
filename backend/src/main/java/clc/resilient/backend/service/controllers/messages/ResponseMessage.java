package clc.resilient.backend.service.controllers.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMessage {
    Boolean success;

    @JsonProperty("status_code")
    Integer statusCode;

    @JsonProperty("status_message")
    String statusMessage;

    Long id;

    List<Object> results;

}
