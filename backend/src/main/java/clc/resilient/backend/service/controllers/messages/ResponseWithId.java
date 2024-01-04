package clc.resilient.backend.service.controllers.messages;

public class ResponseWithId extends ResponseMessage {
    public ResponseWithId(Boolean success, String statusMessage, Long id) {
        setSuccess(success);
        setStatusMessage(statusMessage);
        setId(id);
        setStatusCode(1);
    }
}