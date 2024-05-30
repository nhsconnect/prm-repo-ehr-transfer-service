package uk.nhs.prm.repo.ehrtransferservice.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.builders.ConversationRecordBuilder;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.FailedToPersistException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.DatabaseException;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private ConversationActivityService conversationActivityService;

    @InjectMocks
    private TransferService transferService;

    private final ConversationRecordBuilder conversationRecordBuilder = new ConversationRecordBuilder();

    private static final String NHS_NUMBER = "9798547485";
    private static final String SOURCE_GP = "B45744";
    private static final String NEMS_MESSAGE_ID = "2d74a113-1076-4c63-91bc-e50d232b6a79";
    private static final String DESTINATION_GP = "A74854";
    private static final String NEMS_EVENT_LAST_UPDATED = "2023-10-09T15:38:03.291499328Z";

    @Test
    void createConversation_ValidRepoIncomingEvent_DoesNotThrow() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent repoIncomingEvent = createRepoIncomingEvent(inboundConversationId);

        // then
        assertDoesNotThrow(() -> transferService.createConversation(repoIncomingEvent));
    }

    @Test
    void createConversation_TransferRepositoryThrowsDatabaseException_CreateConversationThrows() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent repoIncomingEvent = createRepoIncomingEvent(inboundConversationId);

        // when
        doThrow(FailedToPersistException.class)
                .when(transferRepository)
                .createConversation(repoIncomingEvent);

        // then
        assertThrows(DatabaseException.class,
                () -> transferService.createConversation(repoIncomingEvent));
    }

    @Test
    void updateConversationTransferStatus_ConversationIsTerminated_DoNotUpdateTransferStatusAndConcludeConversation() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final ConversationTransferStatus currentTransferStatus = INBOUND_COMPLETE;
        final ConversationTransferStatus newTransferStatus = INBOUND_TIMEOUT;
        final ConversationRecord conversationRecord = createConversationRecord(inboundConversationId, currentTransferStatus);


        // when
        when(transferRepository.findConversationByInboundConversationId(inboundConversationId))
                .thenReturn(conversationRecord);

        transferService.updateConversationTransferStatus(inboundConversationId, newTransferStatus);

        // then
        verify(transferRepository, never()).updateConversationStatus(any(), any());
        verify(conversationActivityService).concludeConversationActivity(inboundConversationId);
    }

    @Test
    void updateConversationTransferStatus_ConversationIsPendingAndNewStatusIsPending_UpdateTransferStatusAndDoNotConcludeConversation() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final ConversationTransferStatus currentTransferStatus = INBOUND_STARTED;
        final ConversationTransferStatus newTransferStatus = INBOUND_REQUEST_SENT;
        final ConversationRecord conversationRecord = createConversationRecord(inboundConversationId, currentTransferStatus);

        // when
        when(transferRepository.findConversationByInboundConversationId(inboundConversationId))
                .thenReturn(conversationRecord);

        transferService.updateConversationTransferStatus(inboundConversationId, newTransferStatus);

        // then
        verify(transferRepository).updateConversationStatus(inboundConversationId, newTransferStatus);
        verify(conversationActivityService, never()).concludeConversationActivity(any());
    }

    @Test
    void updateConversationTransferStatus_ConversationIsPendingAndNewStatusIsTerminating_UpdateTransferStatusAndConcludeConversation() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final ConversationTransferStatus currentTransferStatus = INBOUND_REQUEST_SENT;
        final ConversationTransferStatus newTransferStatus = INBOUND_COMPLETE;
        final ConversationRecord conversationRecord = createConversationRecord(inboundConversationId, currentTransferStatus);

        // when
        when(transferRepository.findConversationByInboundConversationId(inboundConversationId))
                .thenReturn(conversationRecord);

        transferService.updateConversationTransferStatus(inboundConversationId, newTransferStatus);

        // then
        verify(transferRepository).updateConversationStatus(inboundConversationId, newTransferStatus);
        verify(conversationActivityService).concludeConversationActivity(inboundConversationId);
    }

    @Test
    void updateConversationTransferStatusWithFailure_ConversationIsTerminated_DoNotUpdateTransferStatusToFailedAndConcludeConversation() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final ConversationTransferStatus currentTransferStatus = INBOUND_COMPLETE;
        final String failureCode = "06";
        final ConversationRecord conversationRecord = createConversationRecord(inboundConversationId, currentTransferStatus);

        // when
        when(transferRepository.findConversationByInboundConversationId(inboundConversationId))
                .thenReturn(conversationRecord);

        transferService.updateConversationTransferStatusWithFailure(inboundConversationId, failureCode);

        // then
        verify(transferRepository, never()).updateConversationStatusWithFailure(any(), any());
        verify(conversationActivityService).concludeConversationActivity(inboundConversationId);
    }

    @Test
    void updateConversationTransferStatusWithFailure_ConversationIsPending_UpdateTransferStatusToFailedAndConcludeConversation() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final ConversationTransferStatus currentTransferStatus = INBOUND_REQUEST_SENT;
        final String failureCode = "06";
        final ConversationRecord conversationRecord = createConversationRecord(inboundConversationId, currentTransferStatus);

        // when
        when(transferRepository.findConversationByInboundConversationId(inboundConversationId))
                .thenReturn(conversationRecord);

        transferService.updateConversationTransferStatusWithFailure(inboundConversationId, failureCode);

        // then
        verify(transferRepository).updateConversationStatusWithFailure(inboundConversationId, failureCode);
        verify(conversationActivityService).concludeConversationActivity(inboundConversationId);
    }


    // Helper Methods
    private ConversationRecord createConversationRecord(
            UUID inboundConversationId,
            ConversationTransferStatus currentTransferStatus
    ) {
        return conversationRecordBuilder
                .withDefaults()
                .withInboundConversationId(inboundConversationId)
                .withTransferStatus(currentTransferStatus)
                .build();
    }

    private RepoIncomingEvent createRepoIncomingEvent(UUID inboundConversationId) {
        return RepoIncomingEvent.builder()
                .nhsNumber(NHS_NUMBER)
                .sourceGp(SOURCE_GP)
                .nemsMessageId(NEMS_MESSAGE_ID)
                .destinationGp(DESTINATION_GP)
                .nemsEventLastUpdated(NEMS_EVENT_LAST_UPDATED)
                .conversationId(inboundConversationId.toString().toUpperCase())
                .build();
    }
}