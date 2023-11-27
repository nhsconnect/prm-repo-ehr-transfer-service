package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferStore;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.EhrResponseFailedException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferTrackerDbException;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.SplunkAuditPublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.SplunkAuditMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepoIncomingServiceTest {

    @Mock
    TransferStore transferStore;

    @Mock
    Gp2gpMessengerService gp2gpMessengerService;

    @Mock
    SplunkAuditPublisher splunkAuditPublisher;

    @Mock
    Integer ehrResponsePollLimit = new Integer(10);

    @InjectMocks
    RepoIncomingService repoIncomingService;

//    @BeforeEach
//    public void setUp() {
//        ReflectionTestUtils.setField(repoIncomingService, "ehrResponsePollLimit", "5");
//        ReflectionTestUtils.setField(repoIncomingService, "ehrResponsePollPeriod", "100");
//    }

    private static final String TRANSFER_STARTED_STATE = "ACTION:TRANSFER_TO_REPO_STARTED";

    @Test
    void shouldMakeInitialDbUpdateWhenRepoIncomingEventReceived() throws Exception {
        var incomingEvent = createIncomingEvent();
        repoIncomingService.processIncomingEvent(incomingEvent);

        verify(transferStore).createEhrTransfer(incomingEvent, "ACTION:TRANSFER_TO_REPO_STARTED");
    }

    @Test
    void shouldCallGp2gpMessengerServiceToSendEhrRequest() throws Exception {
        var incomingEvent = createIncomingEvent();
        repoIncomingService.processIncomingEvent(incomingEvent);

        verify(gp2gpMessengerService).sendEhrRequest(incomingEvent);
    }

    @Test
    void shouldUpdateDbWithEhrRequestSendStatusWhenEhrRequestSentSuccessfully() throws Exception {
        var incomingEvent = createIncomingEvent();
        repoIncomingService.processIncomingEvent(incomingEvent);

        verify(transferStore).handleEhrTransferStateUpdate("conversation-id","nems-message-id", "ACTION:EHR_REQUEST_SENT", true);
    }

    @Test
    void shouldSendMessageToAuditSplunkWithCorrectStatusWhenTransferToRepoIsStarted() throws Exception {
        var incomingEvent = createIncomingEvent();
        var splunkAuditMessage = new SplunkAuditMessage(incomingEvent.getConversationId(), incomingEvent.getNemsMessageId(), "ACTION:TRANSFER_TO_REPO_STARTED");
        repoIncomingService.processIncomingEvent(incomingEvent);

        verify(splunkAuditPublisher).sendMessage(splunkAuditMessage);
    }

    @Test
    void shouldNotSendEhrRequestIsSentMessageToAuditSplunkWhenGp2gpMessengerWhenFailsToMakeInitialDbSave() throws Exception {
        var incomingEvent = createIncomingEvent();
        var splunkAuditMessage = new SplunkAuditMessage(incomingEvent.getConversationId(), incomingEvent.getNemsMessageId(), "ACTION:EHR_REQUEST_SENT");
        doThrow(TransferTrackerDbException.class).when(transferStore).createEhrTransfer(incomingEvent, "ACTION:TRANSFER_TO_REPO_STARTED");

        assertThrows(TransferTrackerDbException.class, () -> repoIncomingService.processIncomingEvent(incomingEvent));
        verify(splunkAuditPublisher, never()).sendMessage(splunkAuditMessage);
    }

    @Test
    void shouldThrowErrorAndNotCallGp2gpMessengerWhenFailsToMakeInitialDbSave() throws Exception {
        var incomingEvent = createIncomingEvent();
        doThrow(TransferTrackerDbException.class).when(transferStore).createEhrTransfer(incomingEvent, "ACTION:TRANSFER_TO_REPO_STARTED");

        assertThrows(TransferTrackerDbException.class, () -> repoIncomingService.processIncomingEvent(incomingEvent));
        verify(gp2gpMessengerService, never()).sendEhrRequest(incomingEvent);
    }

    @Test
    void shouldThrowErrorAndNotUpdateDbWhenFailsToSendEhrRequest() throws Exception {
        var incomingEvent = createIncomingEvent();
        doThrow(Exception.class).when(gp2gpMessengerService).sendEhrRequest(incomingEvent);

        assertThrows(Exception.class, () -> repoIncomingService.processIncomingEvent(incomingEvent));
        verify(transferStore, never()).handleEhrTransferStateUpdate("conversation-id", "nems-message-id" ,"ACTION:EHR_REQUEST_SENT", true);
    }

    @Test
    void shouldWaitForTransferTrackerDbToUpdate() throws Exception {
        // given
        final RepoIncomingEvent repoIncomingEvent = createIncomingEvent();
        final String conversationId = repoIncomingEvent.getConversationId();
        final Transfer transfer = new Transfer(
                conversationId,
                repoIncomingEvent.getNhsNumber(),
                repoIncomingEvent.getSourceGp(),
                repoIncomingEvent.getNemsMessageId(),
                repoIncomingEvent.getNemsEventLastUpdated(),
                TRANSFER_STARTED_STATE,
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                "",
                true
        );

        // when
        when(transferStore.findTransfer(conversationId))
                .thenReturn(transfer);

        repoIncomingService.processIncomingEvent(repoIncomingEvent);

        // then
        assertDoesNotThrow(() -> repoIncomingService.processIncomingEvent(repoIncomingEvent));
    }

    @Test
    void shouldThrowAnEhrResponseFailedExceptionIfStateIsEhrTransferFailed() throws Exception {
        // given
        final RepoIncomingEvent repoIncomingEvent = createIncomingEvent();
        final String conversationId = repoIncomingEvent.getConversationId();
        final Transfer transfer = new Transfer(
                conversationId,
                repoIncomingEvent.getNhsNumber(),
                repoIncomingEvent.getSourceGp(),
                repoIncomingEvent.getNemsMessageId(),
                repoIncomingEvent.getNemsEventLastUpdated(),
                TRANSFER_STARTED_STATE,
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                "",
                true
        );

        // when
        when(transferStore.findTransfer(conversationId))
                .thenReturn(transfer);

        repoIncomingService.processIncomingEvent(repoIncomingEvent);

        // then

    }

    @Test
    void shouldThrowAnEhrResponseFailedExceptionIfStateIsEhrTransferTimeout() {
        // given
        // when
        // then
    }

    @Test
    void shouldThrowAnEhrResponseFailedExceptionIfStateEhrTransferTimeout() {
        // given
        // when
        // then
    }

    @Test
    void shouldThrowAnEhrTimedOutExceptionIfTransferSitsInPendingIndefinitely() {
        // given
        // when
        // then
    }

    /**
     * Utility function to generate a RepoIncomingEvent.
     * @return RepoIncomingEvent
     */
    private RepoIncomingEvent createIncomingEvent() {
        return new RepoIncomingEvent(
                "123456765",
                "source-gp",
                "nems-message-id",
                "destination-gp",
                "last-updated",
                "conversation-id"
        );
    }
}