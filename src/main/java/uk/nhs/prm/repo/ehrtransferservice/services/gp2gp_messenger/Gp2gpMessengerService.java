package uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerEhrRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerPositiveAcknowledgementRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.json_models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

@Service
@RequiredArgsConstructor
@Slf4j
public class Gp2gpMessengerService {
    private final Gp2gpMessengerClient gp2gpMessengerClient;

    @Value("${repositoryAsid}")
    private String repositoryAsid;

    public void sendEhrRequest(RepoIncomingEvent repoIncomingEvent) throws Exception {
        Gp2gpMessengerEhrRequestBody requestBody = new Gp2gpMessengerEhrRequestBody(repoIncomingEvent.getDestinationGp(),
                repositoryAsid, repoIncomingEvent.getSourceGp(), repoIncomingEvent.getConversationId());
        try {
            gp2gpMessengerClient.sendGp2gpMessengerEhrRequest(repoIncomingEvent.getNhsNumber(), requestBody);
            log.info("Successfully sent EHR Request");
        } catch (Exception e) {
            log.error("Caught error during ehr-request");
            throw new Exception("Error while sending ehr-request", e);
        }
    }

    public void sendContinueMessage(ParsedMessage parsedMessage) {
        var conversationId = parsedMessage.getConversationId();
        var messageId = parsedMessage.getMessageId();
        var odsCode = parsedMessage.getOdsCode();
        gp2gpMessengerClient.sendContinueMessage(conversationId, messageId, odsCode);
    }

    public void sendEhrCompletePositiveAcknowledgement(EhrCompleteEvent parsedMessage, TransferTrackerDbEntry ehrTransferData) throws Exception {
        Gp2gpMessengerPositiveAcknowledgementRequestBody requestBody = new Gp2gpMessengerPositiveAcknowledgementRequestBody(repositoryAsid, ehrTransferData.getSourceGP(), parsedMessage.getConversationId().toString(), parsedMessage.getMessageId().toString());
        try {
            gp2gpMessengerClient.sendGp2gpMessengerPositiveAcknowledgement(ehrTransferData.getNhsNumber(), requestBody);
            log.info("Successfully send positive acknowledgement");
        } catch (Exception e) {
            log.error("Caught error sending positive acknowledgement request: " + e.getMessage());
            throw new Exception("Error while sending positive acknowledgement request", e);
        }
    }
}
