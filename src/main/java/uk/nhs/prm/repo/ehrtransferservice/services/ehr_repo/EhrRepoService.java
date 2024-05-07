package uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.PresignedUrl;

import static uk.nhs.prm.repo.ehrtransferservice.utility.InboundTimeoutTracker.captureConversationActivityTimestamp;

@Service
@Slf4j
public class EhrRepoService {
    EhrRepoClient ehrRepoClient;

    public EhrRepoService(EhrRepoClient ehrRepoClient) {
        this.ehrRepoClient = ehrRepoClient;
    }

    public StoreMessageResult storeMessage(ParsedMessage parsedMessage) throws Exception {
        captureConversationActivityTimestamp(parsedMessage.getConversationId());

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
