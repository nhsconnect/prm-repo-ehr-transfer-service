package uk.nhs.prm.repo.ehrtransferservice.database;

import com.amazonaws.SdkClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferRecordNotPresentException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferTrackerDbException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferUnableToPersistException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferUnableToUpdateException;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.SplunkAuditPublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.SplunkAuditMessage;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.STATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final SplunkAuditPublisher splunkAuditPublisher;

    public void createConversation(RepoIncomingEvent event) {
        final UUID inboundConversationId = UUID.fromString(event.getConversationId());

        try {
            transferRepository.createConversation(event);
            log.info("Conversation DynamoDB entry created for Inbound Conversation ID {}.", event.getConversationId());
        } catch (SdkClientException exception) {
            throw new TransferUnableToPersistException(inboundConversationId, exception);
        }
    }

    public String getConversationStatus(UUID inboundConversationId) {
        GetItemResponse response = transferRepository.findConversationByInboundConversationId(inboundConversationId);

        if (response.hasItem()) {
            return response.item().get(STATE.name).s();
        } else {
            throw new TransferRecordNotPresentException(inboundConversationId);
        }
    }

    public boolean isInboundConversationIdPresent(UUID inboundConversationId) {
        return transferRepository
            .findConversationByInboundConversationId(inboundConversationId)
            .hasItem();
    }

    public void updateConversationStatus(UUID inboundConversationId, String nemsMessageId, TransferState state) {
        try {
            transferRepository.updateConversationStatus(inboundConversationId, state);
            log.info("Updated state of EHR transfer in DB to: " + state);
            publishAuditMessage(inboundConversationId.toString(), nemsMessageId, state.name());
        } catch (SdkClientException exception) {
            log.error("Failed to update state of EHR Transfer: " + exception.getMessage());
            throw new TransferUnableToUpdateException(inboundConversationId, exception);
        }
    }

    private void publishAuditMessage(String conversationId, String nemsMessageId, String state) {
        splunkAuditPublisher.sendMessage(new SplunkAuditMessage(conversationId, nemsMessageId, state));
        log.info("Published audit message with the status of: " + state);
    }
}
