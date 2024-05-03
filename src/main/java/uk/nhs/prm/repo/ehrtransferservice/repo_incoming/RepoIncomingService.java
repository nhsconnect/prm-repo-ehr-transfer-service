package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.services.AuditService;
import uk.nhs.prm.repo.ehrtransferservice.services.TimeoutService;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.util.Optional;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_REQUEST_SENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepoIncomingService {
    private final AuditService auditService;
    private final TransferService transferService;
    private final Gp2gpMessengerService gp2gpMessengerService;
    private final TimeoutService timeoutService;

    @Value("${ehrResponsePollLimit}")
    private int ehrResponsePollLimit;

    @Value("${ehrResponsePollPeriodMilliseconds}")
    private int ehrResponsePollPeriodMilliseconds;

    public void processIncomingEvent(RepoIncomingEvent repoIncomingEvent) throws Exception {
        final UUID inboundConversationId = UUID.fromString(repoIncomingEvent.getConversationId());
        final Optional<UUID> nemsMessageId = Optional.of(
            UUID.fromString(repoIncomingEvent.getNemsMessageId())
        );

        transferService.createConversation(repoIncomingEvent);

        gp2gpMessengerService.sendEhrRequest(repoIncomingEvent);
        transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_REQUEST_SENT);

        auditService.publishAuditMessage(inboundConversationId, INBOUND_REQUEST_SENT, nemsMessageId);
        timeoutService.waitForRecordReceived(inboundConversationId);
    }
}