package uk.nhs.prm.repo.ehrtransferservice.exceptions.timeout;

import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.TimeoutException;

import java.util.UUID;

public class TerminatingStatusException extends TimeoutException {
    private static final String EXCEPTION_MESSAGE = "Encountered terminating status for Inbound Conversation ID %s - status is: %s";

    public TerminatingStatusException(UUID inboundConversationId,
                                      ConversationTransferStatus transferStatus) {
        super(EXCEPTION_MESSAGE.formatted(inboundConversationId, transferStatus));
    }
}
