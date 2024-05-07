package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.EhrResponseTimedOutException;
import uk.nhs.prm.repo.ehrtransferservice.services.AuditService;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.*;

@TestPropertySource(locations = "classpath:application.properties")
@ExtendWith(MockitoExtension.class)
class RepoIncomingServiceTest {
    @Mock
    private TransferService transferService;

    @Mock
    private Gp2gpMessengerService gp2gpMessengerService;

    @Mock
    private AuditService auditService;

    @Mock
    private RepoIncomingEvent repoIncomingEvent;

    @InjectMocks
    private RepoIncomingService repoIncomingService;

    private static final UUID INBOUND_CONVERSATION_ID = UUID.fromString("bbd98cce-0a86-44ee-8f51-5267d4d81303");
    private static final UUID NEMS_MESSAGE_ID = UUID.fromString("ce919f71-b1f8-4f4a-82d4-36d75daef1cb");

    @Test
    void processIncomingEvent_ValidRepoIncomingEvent_HandledSuccessfully() throws Exception {
        // when
        when(repoIncomingEvent.getConversationId()).thenReturn(INBOUND_CONVERSATION_ID.toString());
        when(repoIncomingEvent.getNemsMessageId()).thenReturn(NEMS_MESSAGE_ID.toString());
        when(transferService.getConversationTransferStatus(INBOUND_CONVERSATION_ID)).thenReturn(INBOUND_COMPLETE.name());

        repoIncomingService.processIncomingEvent(repoIncomingEvent);

        // then
        verify(transferService).createConversation(repoIncomingEvent);
        verify(gp2gpMessengerService).sendEhrRequest(repoIncomingEvent);
        verify(transferService).updateConversationTransferStatus(INBOUND_CONVERSATION_ID, INBOUND_REQUEST_SENT);
        verify(auditService).publishAuditMessage(INBOUND_CONVERSATION_ID, INBOUND_REQUEST_SENT, Optional.of(NEMS_MESSAGE_ID));
        verify(transferService).getConversationTransferStatus(INBOUND_CONVERSATION_ID);
    }

    @Test
    void processIncomingEvent_SendEhrRequestThrowsException_ShouldNotProceed() throws Exception {
        // given
        final Exception exception = new Exception();

        // when
        when(repoIncomingEvent.getConversationId()).thenReturn(INBOUND_CONVERSATION_ID.toString());
        when(repoIncomingEvent.getNemsMessageId()).thenReturn(NEMS_MESSAGE_ID.toString());
        doThrow(exception)
            .when(gp2gpMessengerService)
            .sendEhrRequest(repoIncomingEvent);

        // then
        assertThrows(Exception.class, () -> repoIncomingService.processIncomingEvent(repoIncomingEvent));
        verify(transferService).createConversation(repoIncomingEvent);
        verify(gp2gpMessengerService).sendEhrRequest(repoIncomingEvent);
        verify(transferService, never()).updateConversationTransferStatus(INBOUND_CONVERSATION_ID, INBOUND_REQUEST_SENT);
        verify(auditService, never()).publishAuditMessage(INBOUND_CONVERSATION_ID, INBOUND_REQUEST_SENT, Optional.of(NEMS_MESSAGE_ID));
        verify(transferService, never()).getConversationTransferStatus(INBOUND_CONVERSATION_ID);
    }

    @Test
    void processIncomingEvent_TransferStatusDoesNotUpdate_TransferStatusUpdatesToInboundTimeoutAndThrowsEhrResponseTimedOutException() throws Exception {
        // when
        configureProcessingParameters(2, 5000);

        when(repoIncomingEvent.getConversationId()).thenReturn(INBOUND_CONVERSATION_ID.toString());
        when(repoIncomingEvent.getNemsMessageId()).thenReturn(NEMS_MESSAGE_ID.toString());
        when(transferService.getConversationTransferStatus(INBOUND_CONVERSATION_ID)).thenReturn(INBOUND_REQUEST_SENT.name());

        // then
        assertThrows(EhrResponseTimedOutException.class, () -> repoIncomingService.processIncomingEvent(repoIncomingEvent));
        verify(transferService).createConversation(repoIncomingEvent);
        verify(gp2gpMessengerService).sendEhrRequest(repoIncomingEvent);
        verify(transferService).updateConversationTransferStatus(INBOUND_CONVERSATION_ID, INBOUND_REQUEST_SENT);
        verify(auditService).publishAuditMessage(INBOUND_CONVERSATION_ID, INBOUND_REQUEST_SENT, Optional.of(NEMS_MESSAGE_ID));
        verify(transferService, times(2)).getConversationTransferStatus(INBOUND_CONVERSATION_ID);
        verify(transferService).updateConversationTransferStatus(INBOUND_CONVERSATION_ID, INBOUND_TIMEOUT);
    }

    private void configureProcessingParameters(int inboundTimeoutSeconds, int pollPeriodMilliseconds)
        throws NoSuchFieldException, IllegalAccessException {
        final Field inboundTimeoutSecondsField = RepoIncomingService.class.getDeclaredField("inboundTimeoutSeconds");
        final Field pollPeriodMillisecondsField = RepoIncomingService.class.getDeclaredField("pollPeriodMilliseconds");

        inboundTimeoutSecondsField.setAccessible(true);
        pollPeriodMillisecondsField.setAccessible(true);

        inboundTimeoutSecondsField.set(this.repoIncomingService, inboundTimeoutSeconds);
        pollPeriodMillisecondsField.set(this.repoIncomingService, pollPeriodMilliseconds);
    }
}