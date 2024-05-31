package uk.nhs.prm.repo.ehrtransferservice.exceptions;

public class ConversationAlreadyInProgressException extends Exception {
    public ConversationAlreadyInProgressException(String message) {
        super(message);
    }
}
