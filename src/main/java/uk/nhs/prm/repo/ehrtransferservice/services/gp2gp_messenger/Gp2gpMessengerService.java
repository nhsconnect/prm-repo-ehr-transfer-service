package uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.HttpException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerContinueMessageRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerEhrRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerPositiveAcknowledgementRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class Gp2gpMessengerService {
    private final Gp2gpMessengerClient gp2gpMessengerClient;
    private final TransferService transferService;

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

    public void sendContinueMessage(ParsedMessage parsedMessage, String sourceGp)
        throws HttpException, IOException, URISyntaxException, InterruptedException {
        Gp2gpMessengerContinueMessageRequestBody continueMessageRequestBody = new Gp2gpMessengerContinueMessageRequestBody(
            parsedMessage.getConversationId(),
            sourceGp,
            parsedMessage.getMessageId()
        );

        gp2gpMessengerClient.sendContinueMessage(continueMessageRequestBody);
        log.info("Continue request sent for Inbound Conversation ID: {}", parsedMessage.getConversationId());
    }

    public void sendEhrCompletePositiveAcknowledgement(UUID inboundConversationId) throws Exception {
        final ConversationRecord record =
            transferService.getConversationByInboundConversationId(inboundConversationId);

        final UUID ehrCoreMessageId =
            transferService.getEhrCoreInboundMessageIdForInboundConversationId(inboundConversationId);

        final var requestBody = new Gp2gpMessengerPositiveAcknowledgementRequestBody(repositoryAsid,
            record.sourceGp(),
            inboundConversationId.toString(),
            ehrCoreMessageId.toString()
        );

        try {
            gp2gpMessengerClient.sendGp2gpMessengerPositiveAcknowledgement(record.nhsNumber(), requestBody);
            log.info("EHR complete positive acknowledgement sent for Inbound Conversation ID: {}", inboundConversationId);
        } catch (Exception exception) {
            log.error("An exception occurred while sending an EHR complete positive acknowledgement: {}", exception.getMessage());
            throw new Exception("Error while sending positive acknowledgement request", exception);
        }
    }
}