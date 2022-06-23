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

import static net.logstash.logback.argument.StructuredArguments.v;

@Service
@RequiredArgsConstructor
@Slf4j
public class NegativeAcknowledgementHandler {

    public static final int FAILURE_INDEX = 0;
    private final TransferTrackerService transferTrackerService;
    private final TransferCompleteMessagePublisher transferCompleteMessagePublisher;

    public void handleMessage(Acknowlegement acknowlegement) throws Exception {
        var conversationId = acknowlegement.getConversationId();
        logFailureDetail(acknowlegement);
        transferTrackerService.updateStateOfEhrTransfer(conversationId.toString(), createState(acknowlegement));
        publishTransferCompleteEvent(transferTrackerService.getEhrTransferData(conversationId.toString()), conversationId);
    }

    private String createState(Acknowlegement acknowlegement) {
        return "ACTION:EHR_TRANSFER_FAILED:" + acknowlegement.getFailureDetails().get(FAILURE_INDEX).code();
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

    private void logFailureDetail(Acknowlegement acknowlegement) {
        acknowlegement.getFailureDetails().forEach(detail ->
                log.info("Negative acknowledgement details",
                        v("acknowledgementTypeCode", acknowlegement.getTypeCode()),
                        v("code", detail.code()),
                        v("displayName", detail.displayName()),
                        v("level", detail.level()),
                        v("codeSystem", detail.codeSystem())));
    }
}
