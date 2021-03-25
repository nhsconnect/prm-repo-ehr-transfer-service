package uk.nhs.prm.deductions.gp2gpmessagehandler.services;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

@Service
public class EhrRepoService {
    EhrRepoClient ehrRepoClient;
    private static Logger logger = LogManager.getLogger(EhrRepoService.class);

    public EhrRepoService(EhrRepoClient ehrRepoClient) {
        this.ehrRepoClient = ehrRepoClient;
    }

    public void storeMessage(ParsedMessage parsedMessage, byte[] messageAsBytes) throws HttpException {
        try {
            PresignedUrl presignedUrl = ehrRepoClient.fetchStorageUrl(parsedMessage.getConversationId(), parsedMessage.getMessageId());
            logger.info("Retrieved Presigned URL");
            presignedUrl.uploadMessage(messageAsBytes);
            logger.info("Uploaded message to S3");
            ehrRepoClient.confirmMessageStored(parsedMessage);
            logger.info("Message stored in EHR Repo");
        } catch (Exception e) {
            throw new HttpException("Failed to store message", e);
        }
    }
}
