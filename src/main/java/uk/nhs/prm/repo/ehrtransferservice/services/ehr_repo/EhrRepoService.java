package uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;
import uk.nhs.prm.repo.ehrtransferservice.services.PresignedUrl;

@Service
@Slf4j
public class EhrRepoService {
    private final EhrRepoClient ehrRepoClient;
    private final ConversationActivityService activityService;

    public EhrRepoService(
        EhrRepoClient ehrRepoClient,
        ConversationActivityService conversationActivityService
    ) {
        this.ehrRepoClient = ehrRepoClient;
        this.activityService = conversationActivityService;
    }

    public StoreMessageResult storeMessage(ParsedMessage parsedMessage) throws Exception {
        activityService.captureConversationActivityTimestamp(parsedMessage.getConversationId());

        PresignedUrl presignedUrl = ehrRepoClient.fetchStorageUrl(parsedMessage.getConversationId(), parsedMessage.getMessageId());
        log.info("Retrieved Presigned URL");
        presignedUrl.uploadMessage(parsedMessage);
        log.info("Uploaded message to S3");
        var confirmedMessageStored = ehrRepoClient.confirmMessageStored(parsedMessage);
        log.info("Message stored in EHR Repo");
        return new StoreMessageResult(confirmedMessageStored);
    }

    public void softDeleteEhrRecord(String nhsNumber) {
        this.ehrRepoClient.softDeleteEhrRecord(nhsNumber);
    }
}
