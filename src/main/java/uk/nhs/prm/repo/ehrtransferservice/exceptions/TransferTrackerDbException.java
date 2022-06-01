package uk.nhs.prm.repo.ehrtransferservice.exceptions;

public class TransferTrackerDbException extends RuntimeException {
    public TransferTrackerDbException(String message, Throwable ex) {
        super(message, ex);
    }

    public TransferTrackerDbException(String message) {
        super(message);
    }
}
