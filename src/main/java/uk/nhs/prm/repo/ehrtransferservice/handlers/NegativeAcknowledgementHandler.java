package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.TransferCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.TransferCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowlegement;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NegativeAcknowledgementHandler {

    public static final int FAILURE_INDEX = 0;
    private final TransferTrackerService transferTrackerService;
    private final TransferCompleteMessagePublisher transferCompleteMessagePublisher;

    public void handleMessage(Acknowlegement acknowlegement) throws Exception {
        var conversationId = acknowlegement.getConversationId();
        transferTrackerService.updateStateOfEhrTransfer(conversationId.toString(), createState(acknowlegement));
        publishTransferCompleteEvent(transferTrackerService.getEhrTransferData(conversationId.toString()), conversationId);
    }

    private String createState(Acknowlegement acknowlegement) {
        var failureDetail = acknowlegement.getFailureDetails().get(FAILURE_INDEX);
        log.info("Failure code is: " + failureDetail.code());
        log.info("Failure detail is: " + failureDetail.displayName());
        return "ACTION:EHR_TRANSFER_FAILED:" + failureDetail.code();
    }

    private void publishTransferCompleteEvent(TransferTrackerDbEntry transferTrackerDbEntry, UUID conversationId) {
        TransferCompleteEvent transferCompleteEvent = new TransferCompleteEvent(
                transferTrackerDbEntry.getNemsEventLastUpdated(),
                transferTrackerDbEntry.getSourceGP(),
                "SUSPENSION",
                transferTrackerDbEntry.getNemsMessageId(),
                transferTrackerDbEntry.getNhsNumber());

        transferCompleteMessagePublisher.sendMessage(transferCompleteEvent, conversationId);

    }
}
