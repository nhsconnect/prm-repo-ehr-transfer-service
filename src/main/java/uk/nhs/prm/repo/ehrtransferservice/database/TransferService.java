package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
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
        transferRepository.createConversation(event);
        log.info("Initial conversation record created for Inbound Conversation ID {}", event.getConversationId());
    }

    public ConversationRecord getConversationByInboundConversationId(UUID inboundConversationId) {
        final ConversationRecord conversationRecord =
            transferRepository.findConversationByInboundConversationId(inboundConversationId);

        log.info("Found conversation record for Inbound Conversation ID {}", inboundConversationId);
        return conversationRecord;
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
        final boolean conversationPresent = transferRepository
            .isInboundConversationPresent(inboundConversationId);

        if (conversationPresent) {
            log.info("Conversation record found for Inbound Conversation ID {}", inboundConversationId);
        } else {
            log.info("Conversation record not found for Inbound Conversation ID {}", inboundConversationId);
        }

        return conversationPresent;
    }

    public void updateConversationTransferStatus(UUID inboundConversationId, ConversationTransferStatus conversationTransferStatus) {
        transferRepository.updateConversationStatus(inboundConversationId, conversationTransferStatus);
        log.info("Updated conversation record with Inbound Conversation ID {} with the status of {}",
            inboundConversationId, conversationTransferStatus.name());
    }

    public void updateConversationTransferStatusWithFailure(UUID inboundConversationId, String failureCode) {
        transferRepository.updateConversationStatusWithFailure(inboundConversationId, failureCode);
        log.info("Updated conversation record with Inbound Conversation ID {} to {}, with failure code {}",
            inboundConversationId, INBOUND_FAILED.name(), failureCode);
    }

    public UUID getEhrCoreInboundMessageIdForInboundConversationId(UUID inboundConversationId) {
        return transferRepository.getEhrCoreInboundMessageIdForInboundConversationId(inboundConversationId);
    }
}