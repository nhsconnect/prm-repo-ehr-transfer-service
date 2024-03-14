package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.EhrResponseFailedException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.EhrResponseTimedOutException;
import uk.nhs.prm.repo.ehrtransferservice.services.AuditService;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.util.Optional;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepoIncomingService {
    private final AuditService auditService;
    private final TransferService transferService;
    private final Gp2gpMessengerService gp2gpMessengerService;

    @Value("${ehrResponsePollLimit}")
    private int ehrResponsePollLimit;

    @Value("${ehrResponsePollPeriodMilliseconds}")
    private int ehrResponsePollPeriodMilliseconds;

    public void processIncomingEvent(RepoIncomingEvent repoIncomingEvent) throws Exception {
        final UUID inboundConversationId = UUID.fromString(repoIncomingEvent.getConversationId());
        final Optional<UUID> nemsMessageId = Optional.of(
            UUID.fromString(repoIncomingEvent.getNemsMessageId())
        );

        if(!transferService.isInboundConversationIdPresent(inboundConversationId))
            transferService.createConversation(repoIncomingEvent);
        else
            log.warn("Inbound Conversation ID {} previously existed, this is a repo incoming message re-read.", inboundConversationId);

        gp2gpMessengerService.sendEhrRequest(repoIncomingEvent);
        transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_REQUEST_SENT);

        auditService.publishAuditMessage(inboundConversationId, INBOUND_REQUEST_SENT, nemsMessageId);
        waitForTransferTrackerDbToUpdate(inboundConversationId);
    }

    private void waitForTransferTrackerDbToUpdate(UUID inboundConversationId)
        throws InterruptedException, EhrResponseFailedException, EhrResponseTimedOutException {
        int pollCount = 0;
        String transferState;

        log.info("Awaiting for transfer status to become INBOUND_COMPLETE for Inbound Conversation ID {}",
            inboundConversationId);

        do {
            Thread.sleep(ehrResponsePollPeriodMilliseconds);
            transferState = transferService.getConversationTransferStatus(inboundConversationId);

            log.info("Transfer status yet to be updated to INBOUND_COMPLETE (currently {}) for Inbound Conversation ID {} - attempt {} of {}",
                transferState,
                inboundConversationId,
                pollCount + 1,
                ehrResponsePollLimit
            );

            if (transferState.equals(INBOUND_COMPLETE.name())) {
                log.info("Transfer status has been set to INBOUND_COMPLETE for Inbound Conversation Id {}", inboundConversationId);
                return;
            }

            pollCount++;
        } while (pollCount < ehrResponsePollLimit);

        log.warn("The transfer for Inbound Conversation ID {} has timed out", inboundConversationId);
        transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_TIMEOUT);
        throw new EhrResponseTimedOutException(transferState);
    }
}