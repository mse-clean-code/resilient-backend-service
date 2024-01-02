package clc.resilient.backend.service.controllers.messages;

public class DeleteListResponse extends ResponseMessage {

    public DeleteListResponse(Boolean success, String statusMessage) {
        setSuccess(success);
        setStatusMessage(statusMessage);
        setStatusCode(13);
    }
}
