package uk.nhs.prm.repo.ehrtransferservice.database;

import com.amazonaws.SdkClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.NemsMessageIdNotPresentException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferUnableToPersistException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferUnableToUpdateException;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.SplunkAuditPublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.SplunkAuditMessage;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.util.UUID;

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

    public ConversationRecord getConversation(UUID inboundConversationId) {
        return transferRepository.findConversationByInboundConversationId(inboundConversationId);
    }

    public String getNemsMessageIdAsString(UUID inboundConversationId) {
        final ConversationRecord conversation = transferRepository
            .findConversationByInboundConversationId(inboundConversationId);

        return String.valueOf(conversation
            .nemsMessageId()
            .orElseThrow(NemsMessageIdNotPresentException::new)
        );
    }

    public String getConversationTransferStatus(UUID inboundConversationId) {
        ConversationRecord conversation = transferRepository
            .findConversationByInboundConversationId(inboundConversationId);

        return conversation.state();
    }

    public boolean isInboundConversationIdPresent(UUID inboundConversationId) {
        return transferRepository.isInboundConversationIdPresent(inboundConversationId);
    }

    public void updateConversationStatus(UUID inboundConversationId, String nemsMessageId, TransferStatus state) {
        try {
            transferRepository.updateConversationStatus(inboundConversationId, state);
            log.info("Updated state of EHR transfer in DB to: {}.", state);
            publishAuditMessage(inboundConversationId.toString(), nemsMessageId, state.name());
        } catch (SdkClientException exception) {
            log.error("Failed to update state of EHR Transfer: " + exception.getMessage());
            throw new TransferUnableToUpdateException(inboundConversationId, exception);
        }
    }

    public void updateConversationStatusWithFailure(
            UUID inboundConversationId,
            String nemsMessageId,
            TransferStatus state,
            String failureCode
    ) {
        try {
            transferRepository.updateConversationStatusWithFailure(inboundConversationId, state, failureCode);
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
