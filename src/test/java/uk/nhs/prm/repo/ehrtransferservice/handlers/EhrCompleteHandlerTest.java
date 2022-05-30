package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.json_models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EhrCompleteHandlerTest {

    @Mock
    EhrCompleteEvent ehrCompleteEvent;

    @Mock
    TransferTrackerService transferTrackerService;

    @Mock
    Gp2gpMessengerService gp2gpMessengerService;

    @Mock
    TransferTrackerDbEntry transferTrackerDbEntry;

    @InjectMocks
    EhrCompleteHandler ehrCompleteHandler;

    @Test
    public void shouldCallGp2gpMessengerServiceToSendPositiveAcknowledgement() {
        var conversationId = UUID.randomUUID();
        when(ehrCompleteEvent.getConversationId()).thenReturn(conversationId);
        when(transferTrackerService.getEhrTransferData(conversationId.toString())).thenReturn(transferTrackerDbEntry);

        ehrCompleteHandler.handleMessage(ehrCompleteEvent);
        verify(gp2gpMessengerService).sendEhrCompletePositiveAcknowledgement(ehrCompleteEvent, transferTrackerDbEntry);
    }
}