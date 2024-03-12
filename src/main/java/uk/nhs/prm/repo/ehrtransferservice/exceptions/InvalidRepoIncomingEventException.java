package uk.nhs.prm.repo.ehrtransferservice.exceptions;

import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.ValidationException;

import java.util.UUID;

public class InvalidRepoIncomingEventException extends ValidationException {
    private static final String EXCEPTION_MESSAGE =
        "The provided Repo Incoming Event is invalid for Inbound Conversation ID %s";

    public InvalidRepoIncomingEventException(UUID inboundConversationId) {
        super(EXCEPTION_MESSAGE.formatted(inboundConversationId.toString()));
    }
}