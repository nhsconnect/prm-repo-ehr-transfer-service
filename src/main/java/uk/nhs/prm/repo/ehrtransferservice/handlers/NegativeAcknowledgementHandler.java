package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class NegativeAcknowledgementHandler {

    private final TransferTrackerService transferTrackerService;

    public void handleMessage(ParsedMessage parsedMessage) {
        var conversationId = parsedMessage.getConversationId();
        var errorCode = extractErrorCode(parsedMessage);

        transferTrackerService.updateStateOfEhrTransfer(conversationId.toString(), errorCode);

    }

    private String extractErrorCode(ParsedMessage parsedMessage) {
        //TODO
        return "ACTION:EHR_TRANSFER_FAILED:${nack-error-here}";
    }

}
