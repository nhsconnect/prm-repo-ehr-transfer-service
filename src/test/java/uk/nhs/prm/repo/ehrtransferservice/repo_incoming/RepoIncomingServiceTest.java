package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RepoIncomingServiceTest {

    @Mock
    TransferTrackerService transferTrackerService;

    @InjectMocks
    RepoIncomingService repoIncomingService;

    @Test
    void shouldParseIncomingEventMessage() {
        var incomingEvent = createIncomingEvent();
        repoIncomingService.processIncomingEvent(incomingEvent);

        verify(transferTrackerService).recordEventInDb(incomingEvent);

    }

    private RepoIncomingEvent createIncomingEvent() {
        return new RepoIncomingEvent("123456765", "source-gp", "nems-message-id", "destination-gp");
    }
}