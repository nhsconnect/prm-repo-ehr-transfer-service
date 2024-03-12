package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeEhrFragmentMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.StoreMessageResult;

@Service
@Slf4j
@RequiredArgsConstructor
public class LargeMessageFragmentHandler implements MessageHandler<ParsedMessage> {

    private final TransferService transferService;

    private final EhrRepoService ehrRepoService;

    @Override
    public void handleMessage(ParsedMessage fragmentMessage) throws Exception {
        StoreMessageResult storeMessageResult = storeFragmentMessage(fragmentMessage);

        if (storeMessageResult.isEhrComplete()) {
            log.info("Successfully stored all fragments of large ehr message in the ehr-repo-service");
            transferService.markConversationAsComplete(fragmentMessage.getConversationId());
        }
    }

    private StoreMessageResult storeFragmentMessage(ParsedMessage fragmentMessage) throws Exception {
        LargeEhrFragmentMessage largeEhrFragment = new LargeEhrFragmentMessage(fragmentMessage);
        StoreMessageResult storeMessageResult = ehrRepoService.storeMessage(largeEhrFragment);
        log.info("Successfully stored one fragment of large ehr message in the ehr-repo-service");
        return storeMessageResult;
    }
}