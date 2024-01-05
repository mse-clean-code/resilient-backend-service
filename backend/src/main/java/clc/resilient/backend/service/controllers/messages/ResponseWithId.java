package clc.resilient.backend.service.controllers.messages;

public class ResponseWithId extends ResponseMessage {
    // Should mimic
    // {"success":true,"status_code":1,"status_message":"Success.","id":8284604}
    public ResponseWithId(Boolean success, String statusMessage, Long id) {
        setSuccess(success);
        setStatusMessage(statusMessage);
        setId(id);
        setStatusCode(1);
    }
}