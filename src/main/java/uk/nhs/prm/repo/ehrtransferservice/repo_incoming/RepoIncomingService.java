package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.services.AuditService;
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

    // the number of times we will poll the TransferTrackerDB to check that we've received the EHR response before timing out
    @Value("${ehrResponsePollLimit}")
    private int ehrResponsePollLimit;

    // how frequently we will poll the TransferTrackerDB to check that we've received the EHR response
    @Value("${ehrResponsePollPeriodMilliseconds}")
    private int ehrResponsePollPeriodMilliseconds;

    public void processIncomingEvent(RepoIncomingEvent repoIncomingEvent) throws Exception {
        final UUID inboundConversationId = UUID.fromString(repoIncomingEvent.getConversationId());
        final UUID nemsMessageId = UUID.fromString(repoIncomingEvent.getNemsMessageId());

        transferService.createConversation(repoIncomingEvent);
        auditService.publishAuditMessage(inboundConversationId, INBOUND_REQUEST_SENT, Optional.of(nemsMessageId));
        gp2gpMessengerService.sendEhrRequest(repoIncomingEvent);

        transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_REQUEST_SENT);
//        waitForTransferTrackerDbToUpdate(repoIncomingEvent.getConversationId());
    }

    // TODO PRMT-4524: DO WE STILL NEED THIS?
//    private void waitForTransferTrackerDbToUpdate(String conversationId) throws InterruptedException, EhrResponseFailedException, EhrResponseTimedOutException {
//        final float timeoutMinutes = ((float) ehrResponsePollLimit * ehrResponsePollPeriodMilliseconds) / (60 * 1000);
//        final float pollLimitSeconds = (float) ehrResponsePollPeriodMilliseconds / 1000;
//        int pollCount = 0;
//        String transferState;
//
//        log.info(String.format(
//                "Polling the TransferTrackerDB every %f seconds up to a maximum of %d times, this could take up to %f minutes",
//                pollLimitSeconds,
//                ehrResponsePollLimit,
//                timeoutMinutes));
//
//        do {
//            Thread.sleep(ehrResponsePollPeriodMilliseconds);
//
//            log.info(String.format(
//                    "Retrieving TransferTrackerDB record for conversationId %s attempt %d of %d",
//                    conversationId,
//                    pollCount + 1,
//                    ehrResponsePollLimit));
//
//            transferState = transferService.getConversationTransferStatus(UUID.fromString(conversationId));
//
//            if (transferState.equals(EHR_SENT_TO_REPOSITORY.name())) {
//                log.info("Successful transfer found for conversationId " + conversationId);
//                return;
//            }
//
//            if (transferState.startsWith(INBOUND_FAILED.name())
//                || transferState.equals(EHR_TRANSFER_TIMEOUT.name())
//            ) {
//                throw new EhrResponseFailedException(transferState);
//            }
//
//            log.info("Still awaiting EHR response for conversationId " + conversationId);
//            pollCount++;
//        } while (pollCount < ehrResponsePollLimit);
//
//        throw new EhrResponseTimedOutException(transferState);
//    }
}
