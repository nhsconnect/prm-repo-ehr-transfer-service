package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowledgement;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.v;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferState.EHR_TRANSFER_FAILED;

@Service
@RequiredArgsConstructor
@Slf4j
public class NegativeAcknowledgementHandler {

    private final TransferService transferService;

    public void handleMessage(Acknowledgement acknowledgement) {
        UUID conversationId = acknowledgement.getConversationId();
        String nemsMessageId = transferService.getNemsMessageIdAsString(conversationId);

        logFailureDetail(acknowledgement);

        transferService.updateConversationStatusWithFailure(
                conversationId,
                nemsMessageId,
                EHR_TRANSFER_FAILED,
                getFailureCodeForDb(acknowledgement));
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
