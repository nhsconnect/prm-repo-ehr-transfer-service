package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.EhrCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeMessageFragments;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageResponseBody;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;

import java.util.UUID;

@Service
@Slf4j
public class LargeMessageFragmentHandler implements MessageHandler<LargeSqsMessage> {

    private final EhrRepoService ehrRepoService;
    private final TransferTrackerService transferTrackerService;
    private final EhrCompleteMessagePublisher ehrCompleteMessagePublisher;

    public LargeMessageFragmentHandler(EhrRepoService ehrRepoService, TransferTrackerService transferTrackerService, EhrCompleteMessagePublisher ehrCompleteMessagePublisher) {
        this.ehrRepoService = ehrRepoService;
        this.transferTrackerService = transferTrackerService;
        this.ehrCompleteMessagePublisher = ehrCompleteMessagePublisher;
    }

    @Override
    public String getInteractionId() {
        return null;
    }

    @Override
    public void handleMessage(LargeSqsMessage largeSqsMessage) throws Exception {
        UUID conversationId = largeSqsMessage.getConversationId();
        var transferTrackerData = transferTrackerService.getEhrTransferData(conversationId.toString());
        var largeMessageFragments = new LargeMessageFragments(largeSqsMessage);
        var storedMessage = ehrRepoService.storeMessage(largeMessageFragments);
        if (isStoredMessageComplete(storedMessage)) {
            publishToEhrCompleteQueue(conversationId, transferTrackerData.getLargeEhrCoreMessageId());
        }
    }

    private void publishToEhrCompleteQueue(UUID conversationId, String messageId) {
        var ehrCompleteEvent = new EhrCompleteEvent(conversationId, UUID.fromString(messageId));
        ehrCompleteMessagePublisher.sendMessage(ehrCompleteEvent);
    }

    private boolean isStoredMessageComplete(StoreMessageResponseBody storedMessage) {
        return storedMessage.getHealthRecordStatus().equals("complete");
    }
}