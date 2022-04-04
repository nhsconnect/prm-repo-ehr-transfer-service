package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RepoIncomingServiceTest {

    @Mock
    TransferTrackerService transferTrackerService;
    @Mock
    Gp2gpMessengerService gp2gpMessengerService;

    @InjectMocks
    RepoIncomingService repoIncomingService;

    @Test
    void shouldMakeInitialDbUpdateWhenRecieveRepoIncomingEvent() throws Exception {
        var incomingEvent = createIncomingEvent();
        repoIncomingService.processIncomingEvent(incomingEvent);

        verify(transferTrackerService).recordEventInDb(incomingEvent, "ACTION:TRANSFER_TO_REPO_STARTED");
    }

    @Test
    void shouldCallGp2gpMessengerServiceToSendEhrRequest() throws Exception {
        var incomingEvent = createIncomingEvent();
        repoIncomingService.processIncomingEvent(incomingEvent);

        verify(gp2gpMessengerService).sendEhrRequest(incomingEvent);
    }


    private RepoIncomingEvent createIncomingEvent() {
        return new RepoIncomingEvent("123456765", "source-gp", "nems-message-id", "destination-gp");
    }
}