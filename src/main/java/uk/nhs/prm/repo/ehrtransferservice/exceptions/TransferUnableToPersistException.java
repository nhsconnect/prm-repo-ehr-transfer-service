package uk.nhs.prm.repo.ehrtransferservice.exceptions;

import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.DatabaseException;

import java.util.UUID;

public class TransferUnableToPersistException extends DatabaseException {
    private static final String EXCEPTION_MESSAGE =
        "Unable to persist transfer to DynamoDB for Inbound Conversation ID %s.";

    public TransferUnableToPersistException(UUID inboundMessageId, Throwable throwable) {
        super(EXCEPTION_MESSAGE.formatted(inboundMessageId.toString()), throwable);
    }
}
