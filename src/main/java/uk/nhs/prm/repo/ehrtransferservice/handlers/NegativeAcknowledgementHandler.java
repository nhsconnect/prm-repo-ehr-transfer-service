package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferStore;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.TransferCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.TransferCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowledgement;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.Transfer;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.v;

@Service
@RequiredArgsConstructor
@Slf4j
public class NegativeAcknowledgementHandler {

    private final TransferStore transferStore;
    private final TransferCompleteMessagePublisher transferCompleteMessagePublisher;

    public void handleMessage(Acknowledgement acknowledgement) {
        var conversationId = acknowledgement.getConversationId();
        boolean isActive = false;

        logFailureDetail(acknowledgement);

        transferStore.handleEhrTransferStateUpdate(
                conversationId.toString(),
                transferStore.findTransfer(conversationId.toString()).getNemsMessageId(),
                createState(acknowledgement),
                isActive);

        publishTransferCompleteEvent(
                transferStore.findTransfer(conversationId.toString()),
                conversationId);
    }

    private String createState(Acknowledgement acknowledgement) {
        return "ACTION:EHR_TRANSFER_FAILED:" + getFailureCodeForDb(acknowledgement);
    }

    private void publishTransferCompleteEvent(Transfer transfer, UUID conversationId) {
        TransferCompleteEvent transferCompleteEvent = new TransferCompleteEvent(
                transfer.getNemsEventLastUpdated(),
                transfer.getSourceGP(),
                "SUSPENSION",
                transfer.getNemsMessageId(),
                transfer.getNhsNumber());

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
