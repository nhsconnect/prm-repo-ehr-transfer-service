package uk.nhs.prm.repo.ehrtransferservice.services;

import jakarta.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.timeout.TerminatingStatusException;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TimeoutService {
    private static final Logger LOGGER = LogManager.getLogger(TimeoutService.class);
    private static final List<ConversationTransferStatus> TERMINATING_STATUSES = List.of(
        ConversationTransferStatus.INBOUND_COMPLETE,
        ConversationTransferStatus.INBOUND_FAILED
    );

    private final TransferService transferService;
    private final Queue<UUID> inboundConversationIds;

    @Autowired
    public TimeoutService(TransferService transferService) {
        this.transferService = transferService;
        this.inboundConversationIds = new LinkedList<>();
    }

    public void add(@Nonnull final UUID inboundConversationId) {
        inboundConversationIds.add(inboundConversationId);
    }

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.MINUTES)
    private void processTimeout() {
        if(inboundConversationIds.isEmpty()) {
            LOGGER.info("There are no timeouts to process.");
        }

        final UUID inboundConversationId = inboundConversationIds.poll();
        terminatingStatusCheck(inboundConversationId);


    }

    /**
     * Checks if the record contains a terminating status,
     * if it does - it'll throw a TerminatingStatusException.
     * @param inboundConversationId The Inbound Conversation ID.
     */
    private void terminatingStatusCheck(UUID inboundConversationId) {
        final String status = transferService.getConversationTransferStatus(inboundConversationId);
        final ConversationTransferStatus conversationTransferStatus = ConversationTransferStatus.valueOf(status);

        if (TERMINATING_STATUSES.contains(conversationTransferStatus)) {
            throw new TerminatingStatusException(inboundConversationId, conversationTransferStatus);
        }
    }
}