package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowlegement;

@Service
@RequiredArgsConstructor
@Slf4j
public class NegativeAcknowledgementHandler {

    private final TransferTrackerService transferTrackerService;

    public void handleMessage(Acknowlegement acknowlegement) {
        var conversationId = acknowlegement.getConversationId();
        var errorCode = extractErrorCode(acknowlegement);

        transferTrackerService.updateStateOfEhrTransfer(conversationId.toString(), errorCode);

    }

    private String extractErrorCode(Acknowlegement acknowlegement) {
        //TODO
        return "ACTION:EHR_TRANSFER_FAILED:${nack-error-here}";
    }

}
