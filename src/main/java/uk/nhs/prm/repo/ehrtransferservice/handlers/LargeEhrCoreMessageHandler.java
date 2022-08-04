package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

@Service
@Slf4j
@RequiredArgsConstructor
public class LargeEhrCoreMessageHandler implements MessageHandler<ParsedMessage> {

    private final EhrRepoService ehrRepoService;
    private final Gp2gpMessengerService gp2gpMessengerService;
    private final TransferTrackerService transferTrackerService;

    @Override
    public void handleMessage(ParsedMessage largeEhrCoreMessage) throws Exception {
        var conversationId = largeEhrCoreMessage.getConversationId();

        ehrRepoService.storeMessage(largeEhrCoreMessage);

        log.info("Successfully stored large-ehr message in the ehr-repo");

        var ehrTransferData = transferTrackerService.getEhrTransferData(conversationId.toString());
        gp2gpMessengerService.sendContinueMessage(largeEhrCoreMessage, ehrTransferData);

        transferTrackerService.handleEhrTransferStateUpdate(conversationId.toString(), ehrTransferData.getNemsMessageId(), "ACTION:LARGE_EHR_CONTINUE_REQUEST_SENT");
        transferTrackerService.updateLargeEhrCoreMessageId(conversationId.toString(), largeEhrCoreMessage.getMessageId().toString());
    }
}