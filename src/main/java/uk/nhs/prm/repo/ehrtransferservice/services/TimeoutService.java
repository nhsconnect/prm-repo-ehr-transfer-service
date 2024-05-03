package uk.nhs.prm.repo.ehrtransferservice.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.timeout.MaxPollException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.timeout.TerminatingStatusException;

import java.util.List;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.*;

@Service
public class TimeoutService {
    private final TransferService transferService;
    private static final Logger LOGGER = LogManager.getLogger(TimeoutService.class);
    private static final List<ConversationTransferStatus> TERMINATING_STATUSES = List.of(
        INBOUND_COMPLETE,
        INBOUND_FAILED
    );

    @Autowired
    public TimeoutService(TransferService transferService) {
        this.transferService = transferService;
    }

    @Retryable(retryFor = MaxPollException.class, maxAttempts = 2)
    public void waitForRecordReceived(final UUID inboundConversationId) {
        terminatingStatusCheck(inboundConversationId);
        setRecordStatusToTimeout(inboundConversationId);
    }

    private void terminatingStatusCheck(UUID inboundConversationId) {
        final String status = transferService.getConversationTransferStatus(inboundConversationId);
        final ConversationTransferStatus conversationTransferStatus = ConversationTransferStatus.valueOf(status);

        if (TERMINATING_STATUSES.contains(conversationTransferStatus)) {
            throw new TerminatingStatusException(inboundConversationId, conversationTransferStatus);
        }
    }

    private void setRecordStatusToTimeout(UUID inboundConversationId) {
        transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_TIMEOUT);
        LOGGER.warn("The record for Inbound Conversation ID {} has timed out.", inboundConversationId);
    }
}