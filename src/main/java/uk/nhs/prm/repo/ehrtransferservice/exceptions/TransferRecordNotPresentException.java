package uk.nhs.prm.repo.ehrtransferservice.exceptions;

import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.DatabaseException;

import java.util.UUID;

public class TransferRecordNotPresentException extends DatabaseException {
    private static final String EXCEPTION_MESSAGE =
        "No transfer present for Inbound Conversation ID %s";

    public TransferRecordNotPresentException(UUID inboundConversationId) {
        super(EXCEPTION_MESSAGE.formatted(inboundConversationId.toString()));
    }
}
