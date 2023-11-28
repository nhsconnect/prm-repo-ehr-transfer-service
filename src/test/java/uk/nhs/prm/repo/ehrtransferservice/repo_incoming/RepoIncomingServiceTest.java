package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import org.apache.commons.lang3.tuple.Pair;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferStore;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.EhrResponseFailedException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.EhrResponseTimedOutException;
import uk.nhs.prm.repo.ehrtransferservice.models.SplunkAuditMessage;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferTrackerDbException;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.SplunkAuditPublisher;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestPropertySource(locations = "classpath:application.properties")
@ExtendWith(MockitoExtension.class)
class RepoIncomingServiceTest {

    @Mock
    TransferStore transferStore;

    @Mock
    Gp2gpMessengerService gp2gpMessengerService;

    @Mock
    SplunkAuditPublisher splunkAuditPublisher;

    @InjectMocks
    RepoIncomingService repoIncomingService;

    private static final String TRANSFER_STARTED_STATE = "ACTION:TRANSFER_TO_REPO_STARTED";
    private static final String TRANSFER_COMPLETE_STATE = "ACTION:EHR_TRANSFER_TO_REPO_COMPLETE";
    private static final String TRANSFER_FAILED_STATE = "ACTION:EHR_TRANSFER_FAILED";
    private static final String TRANSFER_TIMEOUT_STATE = "ACTION:EHR_TRANSFER_TIMEOUT";

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
        final Transfer transfer = createTransfer(repoIncomingEvent, TRANSFER_COMPLETE_STATE);

        // when
        configureEmisTimeouts(10, 10);
        when(transferStore.findTransfer(transfer.getConversationId()))
                .thenReturn(transfer);

        // then
        assertDoesNotThrow(() -> repoIncomingService.processIncomingEvent(repoIncomingEvent));
    }

    @Test
    void shouldThrowAnEhrResponseFailedExceptionIfStateIsEhrTransferFailed() throws Exception {
        // given
        final RepoIncomingEvent repoIncomingEvent = createIncomingEvent();
        final Transfer transfer = createTransfer(repoIncomingEvent, TRANSFER_FAILED_STATE);

        // when
        configureEmisTimeouts(10, 10);
        when(transferStore.findTransfer(transfer.getConversationId()))
                .thenReturn(transfer);

        // then
        assertThrows(EhrResponseFailedException.class,
                () -> repoIncomingService.processIncomingEvent(repoIncomingEvent));
    }

    @Test
    void shouldThrowAnEhrResponseFailedExceptionIfStateIsEhrTimeout() throws Exception {
        // given
        final RepoIncomingEvent repoIncomingEvent = createIncomingEvent();
        final Transfer transfer = createTransfer(repoIncomingEvent, TRANSFER_TIMEOUT_STATE);

        // when
        configureEmisTimeouts(10, 10);
        when(transferStore.findTransfer(transfer.getConversationId()))
                .thenReturn(transfer);

        // then
        assertThrows(EhrResponseFailedException.class,
                () -> repoIncomingService.processIncomingEvent(repoIncomingEvent));
    }

    @Test
    void shouldThrowAnEhrTimedOutExceptionIfTransferSitsInPendingIndefinitely() throws NoSuchFieldException, IllegalAccessException {
        // given
        final RepoIncomingEvent repoIncomingEvent = createIncomingEvent();
        final Transfer transfer = createTransfer(repoIncomingEvent, TRANSFER_STARTED_STATE);

        // when
        configureEmisTimeouts(10, 10);
        when(transferStore.findTransfer(transfer.getConversationId()))
                .thenReturn(transfer);

        // then
        assertThrows(EhrResponseTimedOutException.class,
                () -> repoIncomingService.processIncomingEvent(repoIncomingEvent));
    }

    private Transfer createTransfer(RepoIncomingEvent repoIncomingEvent, String initialState) {
        return new Transfer(
                repoIncomingEvent.getConversationId(),
                repoIncomingEvent.getNhsNumber(),
                repoIncomingEvent.getSourceGp(),
                repoIncomingEvent.getNemsMessageId(),
                repoIncomingEvent.getNemsEventLastUpdated(),
                initialState,
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                UUID.randomUUID().toString(),
                true
        );
    }

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

    private void configureEmisTimeouts(int ehrResponsePollLimit, int ehrResponsePollPeriod) throws NoSuchFieldException, IllegalAccessException {
        final Field ehrResponsePollLimitField = RepoIncomingService.class.getDeclaredField("ehrResponsePollLimit");
        final Field ehrResponsePollPeriodField = RepoIncomingService.class.getDeclaredField("ehrResponsePollPeriod");

        ehrResponsePollLimitField.setAccessible(true);
        ehrResponsePollPeriodField.setAccessible(true);

        ehrResponsePollLimitField.set(this.repoIncomingService, ehrResponsePollLimit);
        ehrResponsePollPeriodField.set(this.repoIncomingService, ehrResponsePollPeriod);
    }
}