package uk.nhs.prm.repo.ehrtransferservice.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EhrResponseTimedOutException extends RuntimeException {
    private static final String BASE_MESSAGE = "EHR Request has remained in a pending state and timed out in " +
            "Transfer status: %s";

    public EhrResponseTimedOutException(String message) {
        super(String.format(BASE_MESSAGE, message));
    }
}