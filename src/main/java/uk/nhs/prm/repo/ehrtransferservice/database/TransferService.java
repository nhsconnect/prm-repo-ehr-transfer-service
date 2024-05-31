package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.ConversationIneligibleForRetryException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.ConversationAlreadyInProgressException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.DatabaseException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.database.ConversationNotPresentException;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;

import java.util.Optional;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.*;

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

    public String getConversationTransferStatus(UUID inboundConversationId) {
        final ConversationRecord conversation =
            getConversationByInboundConversationId(inboundConversationId);

        return conversation.state();
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

    public void updateConversationTransferStatus(UUID inboundConversationId, ConversationTransferStatus conversationTransferStatus) {
        transferRepository.updateConversationStatus(inboundConversationId, conversationTransferStatus);

        if (conversationTransferStatus.isTerminating) {
            activityService.concludeConversationActivity(inboundConversationId);
        }

        log.info("Updated conversation record with Inbound Conversation ID {} with the status of {}",
            inboundConversationId.toString().toUpperCase(), conversationTransferStatus.name());
    }

    public void updateConversationTransferStatusWithFailure(UUID inboundConversationId, String failureCode) {
        transferRepository.updateConversationStatusWithFailure(inboundConversationId, failureCode);

        activityService.concludeConversationActivity(inboundConversationId);

        log.info("Updated conversation record with Inbound Conversation ID {} to {}, with failure code {}",
            inboundConversationId.toString().toUpperCase(), INBOUND_FAILED.name(), failureCode);
    }

    public UUID getEhrCoreInboundMessageIdForInboundConversationId(UUID inboundConversationId) {
        return transferRepository.getEhrCoreInboundMessageIdForInboundConversationId(inboundConversationId);
    }

    public void verifyIfConversationIneligibleForRetry(UUID inboundConversationId)
            throws ConversationAlreadyInProgressException, ConversationIneligibleForRetryException {
        verifyIfConversationAlreadyInProgress(inboundConversationId);

        if (isInboundConversationPresent(inboundConversationId)) {
            verifyIfConversationIsInRetryableTransferStatus(inboundConversationId);
        } else {
            log.info("Processing new RepoIncomingEvent with Inbound Conversation ID: {}", inboundConversationId);
        }

//
//        String transferStatus;
//        try {
//            transferStatus = getConversationTransferStatus(inboundConversationId);
//        } catch (ConversationNotPresentException exception) {
//            log.info("Processing new RepoIncomingEvent with Inbound Conversation ID: {}", inboundConversationId);
//            return false;
//        }
//
//        if (transferStatus == null) {
//            log.info("Processing new RepoIncomingEvent with Inbound Conversation ID: {}", inboundConversationId);
//            return false;
//        } else if (transferStatus.equals(INBOUND_STARTED.name()) || transferStatus.equals(INBOUND_TIMEOUT.name())) {
//            log.info("Retrying RepoIncomingEvent for Inbound Conversation ID: {}", inboundConversationId);
//            return false;
//        } else if (transferStatus.equals(INBOUND_FAILED.name()) || transferStatus.equals(INBOUND_COMPLETE.name()) || transferStatus.contains("OUTBOUND_")) {
//            log.warn("RepoIncomingEvent is not eligible for retry for Inbound Conversation ID: {}", inboundConversationId);
//            return true;
//        } else if (transferStatus.equals(INBOUND_REQUEST_SENT.name()) || transferStatus.equals(INBOUND_CORE_RECEIVED.name()) || transferStatus.equals(INBOUND_CONTINUE_REQUEST_SENT.name())) {
//            // TODO: COMMENT THIS IF STATEMENT OUT!
//        }
//        return true;
    }

    private void verifyIfConversationIsInRetryableTransferStatus(UUID inboundConversationId) throws ConversationIneligibleForRetryException {
        String transferStatus = getConversationTransferStatus(inboundConversationId);

        if (transferStatus.equals(INBOUND_FAILED.name()) ||
            transferStatus.equals(INBOUND_COMPLETE.name()) ||
            transferStatus.startsWith("OUTBOUND_")
        ) {
            throw new ConversationIneligibleForRetryException(inboundConversationId);
        }
        log.info("Retrying RepoIncomingEvent for Inbound Conversation ID: {}", inboundConversationId);
    }

    private void verifyIfConversationAlreadyInProgress(UUID inboundConversationId) throws ConversationAlreadyInProgressException {
        if (activityService.isConversationActive(inboundConversationId)) {
            if (activityService.isConversationTimedOut(inboundConversationId)) {
                log.warn("On conversation being retried with Inbound Conversation ID: {}, found active transfer that should have already timed out", inboundConversationId);
                activityService.concludeConversationActivity(inboundConversationId);
            } else {
                throw new ConversationAlreadyInProgressException("Transfer is already in progress.");
            }
        }
    }
}