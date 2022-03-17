package uk.nhs.prm.deductions.gp2gpmessagehandler.services;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

@Service
@Slf4j
public class EhrRepoService {
    EhrRepoClient ehrRepoClient;

    public EhrRepoService(EhrRepoClient ehrRepoClient) {
        this.ehrRepoClient = ehrRepoClient;
    }

    public void storeMessage(ParsedMessage parsedMessage) throws HttpException {
        try {
            PresignedUrl presignedUrl = ehrRepoClient.fetchStorageUrl(parsedMessage.getConversationId(), parsedMessage.getMessageId());
            log.info("Retrieved Presigned URL");
            presignedUrl.uploadMessage(parsedMessage);
            log.info("Uploaded message to S3");
            ehrRepoClient.confirmMessageStored(parsedMessage);
            log.info("Message stored in EHR Repo");
        } catch (Exception e) {
            throw new HttpException("Failed to store message", e);
        }
    }
}
