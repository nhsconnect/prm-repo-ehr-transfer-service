package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class LargeMessageFragmentHandler implements MessageHandler<LargeSqsMessage> {

    private final EhrRepoService ehrRepoService;
    private final TransferTrackerService transferTrackerService;
    private final EhrCompleteMessagePublisher ehrCompleteMessagePublisher;

    @Override
    public void handleMessage(LargeSqsMessage largeSqsMessage) throws Exception {
        if (isStoredMessageComplete(storeLargeMessageFragments(largeSqsMessage))) {
            log.info("Successfully stored all fragments of large ehr message in the ehr-repo-service");
            publishToEhrCompleteQueue(largeSqsMessage.getConversationId());
        }
    }

    private StoreMessageResponseBody storeLargeMessageFragments(LargeSqsMessage largeSqsMessage) throws Exception {
        var largeMessageFragments = new LargeMessageFragments(largeSqsMessage);
        var storedMessage = ehrRepoService.storeMessage(largeMessageFragments);
        log.info("Successfully stored one fragment of large ehr message in the ehr-repo-service");
        return storedMessage;
    }

    private void publishToEhrCompleteQueue(UUID conversationId) {
        var transferTrackerData = transferTrackerService.getEhrTransferData(conversationId.toString());
        var ehrCompleteEvent = new EhrCompleteEvent(conversationId, UUID.fromString(transferTrackerData.getLargeEhrCoreMessageId()));
        ehrCompleteMessagePublisher.sendMessage(ehrCompleteEvent);
        log.info("Published all of the large ehr fragments messages to ehr-complete topic");
    }

    private boolean isStoredMessageComplete(StoreMessageResponseBody storedMessage) {
        return storedMessage.getHealthRecordStatus().equals("complete");
    }
}