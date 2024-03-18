package uk.nhs.prm.repo.ehrtransferservice.exceptions;

import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.DatabaseException;

import java.util.UUID;

public class UpdateConversationException extends DatabaseException {
    private static final String EXCEPTION_MESSAGE =
        "The conversation could not be updated with Inbound Conversation ID %s";

    public UpdateConversationException(UUID inboundConversationId) {
        super(EXCEPTION_MESSAGE.formatted(inboundConversationId));
    }
}
