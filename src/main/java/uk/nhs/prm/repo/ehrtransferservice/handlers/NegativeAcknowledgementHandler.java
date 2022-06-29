package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.TransferCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.TransferCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowledgement;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.v;

@Service
@RequiredArgsConstructor
@Slf4j
public class NegativeAcknowledgementHandler {

    private final TransferTrackerService transferTrackerService;
    private final TransferCompleteMessagePublisher transferCompleteMessagePublisher;

    public void handleMessage(Acknowledgement acknowledgement) throws Exception {
        var conversationId = acknowledgement.getConversationId();
        logFailureDetail(acknowledgement);
        transferTrackerService.updateStateOfEhrTransfer(conversationId.toString(), createState(acknowledgement));
        publishTransferCompleteEvent(transferTrackerService.getEhrTransferData(conversationId.toString()), conversationId);
    }

    private String createState(Acknowledgement acknowledgement) {
        return "ACTION:EHR_TRANSFER_FAILED:" + getFailureCodeForDb(acknowledgement);
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

    private String getFailureCodeForDb(Acknowledgement acknowledgement) {
        if (acknowledgement.getFailureDetails().isEmpty()) {
            return "UNKNOWN_ERROR";
        }
        return acknowledgement.getFailureDetails().get(0).code();
    }

    private void logFailureDetail(Acknowledgement acknowledgement) {
        acknowledgement.getFailureDetails().forEach(detail ->
                log.info("Negative acknowledgement details",
                        v("acknowledgementTypeCode", acknowledgement.getTypeCode()),
                        v("detail.code", detail.code()),
                        v("detail.displayName", detail.displayName()),
                        v("detail.level", detail.level()),
                        v("detail.codeSystem", detail.codeSystem())));
    }
}
