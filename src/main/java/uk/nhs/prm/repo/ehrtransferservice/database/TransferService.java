package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.DatabaseException;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;

import java.util.Optional;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_FAILED;

@Log4j2
@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;
    private final ConversationActivityService activityService;

    public void createConversation(RepoIncomingEvent event) {
        try {
            transferRepository.createConversation(event);
            log.info("Initial conversation record created for Inbound Conversation ID {}", event.getConversationId());
        } catch (DatabaseException exception) {
            log.warn(exception.getMessage());
            throw exception;
        }
    }

    public ConversationRecord getConversationByInboundConversationId(UUID inboundConversationId) {
        final ConversationRecord conversationRecord =
            transferRepository.findConversationByInboundConversationId(inboundConversationId);

        log.info("Found conversation record for Inbound Conversation ID {}", inboundConversationId.toString().toUpperCase());
        return conversationRecord;
    }

    public Optional<UUID> getNemsMessageIdAsUuid(UUID inboundConversationId) {
        final ConversationRecord conversation = getConversationByInboundConversationId(inboundConversationId);
        return conversation.nemsMessageId();
    }

    public ConversationTransferStatus getConversationTransferStatus(UUID inboundConversationId) {
        return getConversationByInboundConversationId(inboundConversationId).transferStatus();
    }

    public boolean isInboundConversationPresent(UUID inboundConversationId) {
        final boolean conversationPresent = transferRepository
            .isInboundConversationPresent(inboundConversationId);

        if (conversationPresent) {
            log.info("Conversation record found for Inbound Conversation ID {}", inboundConversationId.toString().toUpperCase());
        } else {
            log.info("Conversation record not found for Inbound Conversation ID {}", inboundConversationId.toString().toUpperCase());
        }

        return conversationPresent;
    }

    public void updateConversationTransferStatus(UUID inboundConversationId, ConversationTransferStatus newTransferStatus) {
        ConversationTransferStatus currentTransferStatus = getConversationTransferStatus(inboundConversationId);

        if (currentTransferStatus.isTerminating) {
            rejectConversationTransferStatusUpdateForTerminatedConversation(inboundConversationId, newTransferStatus, currentTransferStatus);
        } else {
            updateConversationTransferStatusForPendingConversation(inboundConversationId, newTransferStatus);
        }
    }

    private void rejectConversationTransferStatusUpdateForTerminatedConversation(
            UUID inboundConversationId,
            ConversationTransferStatus newTransferStatus,
            ConversationTransferStatus currentTransferStatus
    ) {
        log.warn("Cannot update conversation record with Inbound Conversation ID {} to new status {} as the " +
                        "conversation has already terminated with status {}",
                inboundConversationId.toString().toUpperCase(),
                newTransferStatus.name(),
                currentTransferStatus.name());

        // the conversation activity should already be concluded, but just in case
        activityService.concludeConversationActivity(inboundConversationId);
    }

    private void updateConversationTransferStatusForPendingConversation(
            UUID inboundConversationId,
            ConversationTransferStatus newTransferStatus
    ) {
        transferRepository.updateConversationStatus(inboundConversationId, newTransferStatus);

        log.info("Updated conversation record with Inbound Conversation ID {} with TransferStatus of {}",
                inboundConversationId.toString().toUpperCase(), newTransferStatus.name());

        if (newTransferStatus.isTerminating) {
            activityService.concludeConversationActivity(inboundConversationId);
        }
    }

    public void updateConversationTransferStatusWithFailure(UUID inboundConversationId, String failureCode) {
        ConversationTransferStatus currentTransferStatus = getConversationTransferStatus(inboundConversationId);

        if (currentTransferStatus.isTerminating) {
            rejectConversationTransferStatusUpdateForTerminatedConversation(inboundConversationId, INBOUND_FAILED, currentTransferStatus);
        } else {
            transferRepository.updateConversationStatusWithFailure(inboundConversationId, failureCode);

            activityService.concludeConversationActivity(inboundConversationId);

            log.info("Updated conversation record with Inbound Conversation ID {} to {}, with failure code {}",
                    inboundConversationId.toString().toUpperCase(), INBOUND_FAILED.name(), failureCode);
        }
    }

    public UUID getEhrCoreInboundMessageIdForInboundConversationId(UUID inboundConversationId) {
        return transferRepository.getEhrCoreInboundMessageIdForInboundConversationId(inboundConversationId);
    }
}