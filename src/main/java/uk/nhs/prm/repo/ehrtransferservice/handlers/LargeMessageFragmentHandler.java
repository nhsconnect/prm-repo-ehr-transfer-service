package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferStore;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.EhrCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeEhrFragmentMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.StoreMessageResult;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LargeMessageFragmentHandler implements MessageHandler<ParsedMessage> {

    private final EhrRepoService ehrRepoService;
    private final TransferStore transferStore;
    private final EhrCompleteMessagePublisher ehrCompleteMessagePublisher;

    @Override
    public void handleMessage(ParsedMessage fragmentMessage) throws Exception {
        var storeMessageResult = storeFragmentMessage(fragmentMessage);
        if (storeMessageResult.isEhrComplete()) {
            log.info("Successfully stored all fragments of large ehr message in the ehr-repo-service");
            publishToEhrCompleteQueue(fragmentMessage.getConversationId());
        }
    }

    private StoreMessageResult storeFragmentMessage(ParsedMessage fragmentMessage) throws Exception {
        var largeEhrFragment = new LargeEhrFragmentMessage(fragmentMessage);
        var storeMessageResult = ehrRepoService.storeMessage(largeEhrFragment);
        log.info("Successfully stored one fragment of large ehr message in the ehr-repo-service");
        return storeMessageResult;
    }

    private void publishToEhrCompleteQueue(UUID conversationId) {
        var transfer = transferStore.findTransfer(conversationId);
        var ehrCompleteEvent = new EhrCompleteEvent(conversationId, UUID.fromString(transfer.getLargeEhrCoreMessageId()));
        ehrCompleteMessagePublisher.sendMessage(ehrCompleteEvent);
        log.info("Published all of the large ehr fragments messages to ehr-complete topic");
    }

}