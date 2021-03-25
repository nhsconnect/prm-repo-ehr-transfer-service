package uk.nhs.prm.deductions.gp2gpmessagehandler.services;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

@Service
public class EhrRepoService {
    EhrRepoClient ehrRepoClient;

    public EhrRepoService(EhrRepoClient ehrRepoClient) {
        this.ehrRepoClient = ehrRepoClient;
    }

    public void storeMessage(ParsedMessage parsedMessage, byte[] messageAsBytes) throws HttpException {
        try {
            PresignedUrl presignedUrl = ehrRepoClient.fetchStorageUrl(parsedMessage.getConversationId(), parsedMessage.getMessageId());
            presignedUrl.uploadMessage(messageAsBytes);
            ehrRepoClient.confirmMessageStored(parsedMessage);
        } catch (Exception e) {
            throw new HttpException("Failed to store message", e);
        }
    }
}
