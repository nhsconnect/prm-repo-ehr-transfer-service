package uk.nhs.prm.repo.ehrtransferservice.exceptions;

import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.DatabaseException;

import java.util.UUID;

public class ConversationAlreadyPresentException extends DatabaseException {
    private static final String EXCEPTION_MESSAGE =
        "The conversation already exists for Inbound Message ID %s";

    public ConversationAlreadyPresentException(UUID inboundConversationId) {
        super(EXCEPTION_MESSAGE.formatted(inboundConversationId));
    }
}