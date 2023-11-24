package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferStore;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.EhrResponseFailedException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.EhrResponseTimedOutException;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.SplunkAuditPublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.SplunkAuditMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepoIncomingService {

    private static final String TRANSFER_TO_REPO_STARTED = "ACTION:TRANSFER_TO_REPO_STARTED";
    private static final String EHR_REQUEST_SENT = "ACTION:EHR_REQUEST_SENT";

    private final TransferStore transferStore;
    private final SplunkAuditPublisher splunkAuditPublisher;
    private final Gp2gpMessengerService gp2gpMessengerService;

    @Value("${ehrResponsePollLimit}")
    private int ehrResponsePollLimit;

    @Value("${ehrResponsePollPeriodMilliseconds}")
    private int ehrResponsePollPeriod;

    public void processIncomingEvent(RepoIncomingEvent repoIncomingEvent) throws Exception {
        boolean isActive = true;
        transferStore.createEhrTransfer(repoIncomingEvent, TRANSFER_TO_REPO_STARTED);
        splunkAuditPublisher.sendMessage(new SplunkAuditMessage(repoIncomingEvent.getConversationId(),repoIncomingEvent.getNemsMessageId(),TRANSFER_TO_REPO_STARTED));
        gp2gpMessengerService.sendEhrRequest(repoIncomingEvent);
        transferStore.handleEhrTransferStateUpdate(repoIncomingEvent.getConversationId(), repoIncomingEvent.getNemsMessageId(), EHR_REQUEST_SENT, isActive);
        waitForTransferTrackerDbToUpdate(repoIncomingEvent.getConversationId());
    }

    private void waitForTransferTrackerDbToUpdate(String conversationId)
            throws InterruptedException, EhrResponseFailedException, EhrResponseTimedOutException {
        int pollCount = 0;
        String transferState = "";

        while (pollCount < ehrResponsePollLimit) {
            Thread.sleep(ehrResponsePollPeriod);

            log.info("Retrieving TransferTrackerDB record for conversationId " + conversationId);
            Transfer transfer = transferStore.findTransfer(conversationId);
            transferState = transfer.getState();

            if (transferState.equals("ACTION:EHR_TRANSFER_TO_REPO_COMPLETE")) {
                return;
            }

            if (transferState.startsWith("ACTION:EHR_TRANSFER_FAILED")
                || transferState.equals("ACTION:EHR_TRANSFER_TIMEOUT")
            ) {
                throw new EhrResponseFailedException(transferState);
            }

            log.info("Still awaiting EHR response for conversationId " + conversationId);
            pollCount++;
        }

        throw new EhrResponseTimedOutException(transferState);
    }
}
