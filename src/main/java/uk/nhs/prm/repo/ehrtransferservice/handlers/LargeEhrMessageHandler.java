package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.EhrCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;

@Service
@Slf4j
public class LargeEhrMessageHandler implements MessageHandler {

    private EhrRepoService ehrRepoService;
    private EhrCompleteMessagePublisher ehrCompleteMessagePublisher;

    public LargeEhrMessageHandler(EhrRepoService ehrRepoService, EhrCompleteMessagePublisher ehrCompleteMessagePublisher) {
        this.ehrRepoService = ehrRepoService;
        this.ehrCompleteMessagePublisher = ehrCompleteMessagePublisher;
    }

    @Override
    public String getInteractionId() {
        return null;
    }

    @Override
    public void handleMessage(ParsedMessage parsedMessage) throws Exception {
        //call GP2GP messenger to send continue request
       ehrRepoService.storeMessage(parsedMessage);
        log.info("Successfully stored small-ehr message in the ehr-repo-service");
       ehrCompleteMessagePublisher.sendMessage(new EhrCompleteEvent(parsedMessage.getConversationId(), parsedMessage.getMessageId()));
        log.info("Successfully published message to ehr-complete topic");
    }
}