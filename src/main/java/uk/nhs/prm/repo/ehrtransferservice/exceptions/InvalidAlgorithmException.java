package uk.nhs.prm.repo.ehrtransferservice.exceptions;

public class InvalidAlgorithmException extends RuntimeException {
    private static final String EXCEPTION_MESSAGE = "An invalid algorithm was provided, details: %s.";
    public InvalidAlgorithmException(String details) {
        super(EXCEPTION_MESSAGE.formatted(details));
    }
}
