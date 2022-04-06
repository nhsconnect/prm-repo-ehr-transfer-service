package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferTrackerDbException;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepoIncomingServiceTest {

    @Mock
    TransferTrackerService transferTrackerService;
    @Mock
    Gp2gpMessengerService gp2gpMessengerService;

    @InjectMocks
    RepoIncomingService repoIncomingService;

    @Test
    void shouldMakeInitialDbUpdateWhenRepoIncomingEventReceived() throws Exception {
        var incomingEvent = createIncomingEvent();
        repoIncomingService.processIncomingEvent(incomingEvent);

        verify(transferTrackerService).createEhrTransfer(incomingEvent, "ACTION:TRANSFER_TO_REPO_STARTED");
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

        verify(transferTrackerService).updateStateOfEhrTransfer("conversation-id","ACTION:EHR_REQUEST_SENT");
    }

    @Test
    void shouldThrowErrorAndNotCallGp2gpMessengerWhenFailsToMakeInitialDbSave() throws Exception {
        var incomingEvent = createIncomingEvent();
        doThrow(TransferTrackerDbException.class).when(transferTrackerService).createEhrTransfer(incomingEvent, "ACTION:TRANSFER_TO_REPO_STARTED");

        assertThrows(TransferTrackerDbException.class, () -> repoIncomingService.processIncomingEvent(incomingEvent));
        verify(gp2gpMessengerService, never()).sendEhrRequest(incomingEvent);
    }

    @Test
    void shouldThrowErrorAndNotUpdateDbWhenFailsToSendEhrRequest() throws Exception {
        var incomingEvent = createIncomingEvent();
        doThrow(Exception.class).when(gp2gpMessengerService).sendEhrRequest(incomingEvent);

        assertThrows(Exception.class, () -> repoIncomingService.processIncomingEvent(incomingEvent));
        verify(transferTrackerService, never()).updateStateOfEhrTransfer("conversation-id","ACTION:EHR_REQUEST_SENT");
    }


    private RepoIncomingEvent createIncomingEvent() {
        return new RepoIncomingEvent("123456765", "source-gp", "nems-message-id", "destination-gp", "conversation-id");
    }
}