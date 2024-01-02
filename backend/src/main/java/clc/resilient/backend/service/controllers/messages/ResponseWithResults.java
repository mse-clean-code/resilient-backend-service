package clc.resilient.backend.service.controllers.messages;

import java.util.List;

public class ResponseWithResults extends ResponseMessage {

    public ResponseWithResults(Boolean success, String statusMessage, List<Object> results) {
        setSuccess(success);
        setStatusMessage(statusMessage);
        setResults(results);
        setStatusCode(1);
    }
}
