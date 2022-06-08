package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.EhrCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeEhrMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

@Service
@Slf4j
public class LargeEhrMessageHandler implements MessageHandler<LargeEhrMessage> {

    private EhrRepoService ehrRepoService;
    private EhrCompleteMessagePublisher ehrCompleteMessagePublisher;
    private Gp2gpMessengerService gp2gpMessengerService;
    private TransferTrackerService transferTrackerService;

    public LargeEhrMessageHandler(EhrRepoService ehrRepoService, EhrCompleteMessagePublisher ehrCompleteMessagePublisher, Gp2gpMessengerService gp2gpMessengerService, TransferTrackerService transferTrackerService) {
        this.ehrRepoService = ehrRepoService;
        this.ehrCompleteMessagePublisher = ehrCompleteMessagePublisher;
        this.gp2gpMessengerService = gp2gpMessengerService;
        this.transferTrackerService = transferTrackerService;
    }

    @Override
    public String getInteractionId() {
        return null;
    }

    @Override
    public void handleMessage(LargeEhrMessage largeEhrMessage) throws Exception {
        var conversationId = largeEhrMessage.getConversationId();

        ehrRepoService.storeMessage(largeEhrMessage);
        log.info("Successfully stored large-ehr message in the ehr-repo");

        var ehrTransferData = transferTrackerService.getEhrTransferData(conversationId.toString());
        gp2gpMessengerService.sendContinueMessage(largeEhrMessage, ehrTransferData);

        transferTrackerService.updateStateOfEhrTransfer(conversationId.toString(), "ACTION:LARGE_EHR_CONTINUE_REQUEST_SENT");
    }
}