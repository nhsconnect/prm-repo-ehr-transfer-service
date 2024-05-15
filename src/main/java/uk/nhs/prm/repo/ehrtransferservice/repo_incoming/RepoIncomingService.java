package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.timeout.TimeoutExceededException;
import uk.nhs.prm.repo.ehrtransferservice.services.AuditService;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.util.Optional;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_REQUEST_SENT;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_TIMEOUT;

@Slf4j
@Service
public class RepoIncomingService {
    private final AuditService auditService;
    private final TransferService transferService;
    private final Gp2gpMessengerService gp2gpMessengerService;
    private final ConversationActivityService conversationActivityService;

    @Value("${ehrTransferFinalisedPollPeriodMilliseconds}")
    private int pollPeriodMilliseconds;

    @Autowired
    public RepoIncomingService(
        AuditService auditService,
        TransferService transferService,
        Gp2gpMessengerService gp2gpMessengerService,
        ConversationActivityService conversationActivityService
    ) {
        this.auditService = auditService;
        this.transferService = transferService;
        this.gp2gpMessengerService = gp2gpMessengerService;
        this.conversationActivityService = conversationActivityService;
    }

    public void processIncomingEvent(RepoIncomingEvent repoIncomingEvent) throws Exception {
        final UUID inboundConversationId = UUID.fromString(repoIncomingEvent.getConversationId());
        final Optional<UUID> nemsMessageId = Optional.of(
            UUID.fromString(repoIncomingEvent.getNemsMessageId())
        );

        conversationActivityService.captureConversationActivityTimestamp(inboundConversationId);

        transferService.createConversation(repoIncomingEvent);

        gp2gpMessengerService.sendEhrRequest(repoIncomingEvent);
        transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_REQUEST_SENT);

        auditService.publishAuditMessage(inboundConversationId, INBOUND_REQUEST_SENT, nemsMessageId);
        waitForConversationToComplete(inboundConversationId);
    }

    private void waitForConversationToComplete(UUID inboundConversationId) throws InterruptedException {
        log.info("Awaiting Inbound Conversation ID {} to complete", inboundConversationId.toString().toUpperCase());

        while (conversationActivityService.isConversationActive(inboundConversationId)) {
            Thread.sleep(pollPeriodMilliseconds);
            verifyConversationNotTimedOut(inboundConversationId);
        }
    }

    private void verifyConversationNotTimedOut(UUID inboundConversationId) {
        if (conversationActivityService.isConversationTimedOut(inboundConversationId)) {
            transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_TIMEOUT);
            throw new TimeoutExceededException(inboundConversationId);
        }
    }
}