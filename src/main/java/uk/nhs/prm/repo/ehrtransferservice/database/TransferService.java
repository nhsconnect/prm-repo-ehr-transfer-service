package uk.nhs.prm.repo.ehrtransferservice.database;

import com.amazonaws.SdkClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.FailedToPersistException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferUnableToUpdateException;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.util.Optional;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_FAILED;

@Log4j2
@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;

    public void createConversation(RepoIncomingEvent event) {
        final UUID inboundConversationId = UUID.fromString(event.getConversationId());

        try {
            transferRepository.createConversation(event);
            log.info("Initial Conversation created for Inbound Conversation ID {}.", event.getConversationId());
        } catch (SdkClientException exception) {
            throw new FailedToPersistException(inboundConversationId, exception);
        }
    }

    public ConversationRecord getConversationByInboundConversationId(UUID inboundConversationId) {
        return transferRepository.findConversationByInboundConversationId(inboundConversationId);
    }

    public Optional<UUID> getNemsMessageIdAsUuid(UUID inboundConversationId) {
        final ConversationRecord conversation = transferRepository
            .findConversationByInboundConversationId(inboundConversationId);

        return conversation.nemsMessageId();
    }

    public String getConversationTransferStatus(UUID inboundConversationId) {
        ConversationRecord conversation = transferRepository
            .findConversationByInboundConversationId(inboundConversationId);

        return conversation.state();
    }

    public boolean isInboundConversationIdPresent(UUID inboundConversationId) {
        return transferRepository.isInboundConversationPresent(inboundConversationId);
    }

    public void updateConversationTransferStatus(UUID inboundConversationId, ConversationTransferStatus conversationTransferStatus) {
        try {
            transferRepository.updateConversationStatus(inboundConversationId, conversationTransferStatus);
            log.info("Updated conversationTransferStatus of EHR transfer in DB to: {}.", conversationTransferStatus);
        } catch (SdkClientException exception) {
            log.error("Failed to update conversationTransferStatus of EHR Transfer: " + exception.getMessage());
            throw new TransferUnableToUpdateException(inboundConversationId, exception);
        }
    }

    public void updateConversationTransferStatusWithFailure(UUID inboundConversationId, String failureCode) {
        try {
            transferRepository.updateConversationStatusWithFailure(inboundConversationId, failureCode);
            log.info("Updated conversationTransferStatus of EHR transfer in DB to: {}", INBOUND_FAILED);
        } catch (SdkClientException exception) {
            log.error("Failed to update conversationTransferStatus of EHR Transfer: " + exception.getMessage());
            throw new TransferUnableToUpdateException(inboundConversationId, exception);
        }
    }

    public UUID getEhrCoreInboundMessageIdForInboundConversationId(UUID inboundConversationId) {
        return transferRepository
            .getEhrCoreInboundMessageIdForInboundConversationId(inboundConversationId);
    }
}