package uk.nhs.prm.repo.ehrtransferservice.exceptions;

import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.DatabaseException;

import java.util.UUID;

public class TransferUpdateFailedException extends DatabaseException {
    private static final String EXCEPTION_MESSAGE =
        "Unable to update transfer record for Inbound Conversation ID %s.";

    public TransferUpdateFailedException(UUID inboundConversationId, Throwable throwable) {
        super(EXCEPTION_MESSAGE.formatted(inboundConversationId.toString()), throwable);
    }
}
