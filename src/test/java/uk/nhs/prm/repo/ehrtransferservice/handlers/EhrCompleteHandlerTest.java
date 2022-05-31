package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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

    UUID conversationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(ehrCompleteEvent.getConversationId()).thenReturn(conversationId);
        when(transferTrackerService.getEhrTransferData(conversationId.toString())).thenReturn(transferTrackerDbEntry);
    }

    @Test
    public void shouldCallGp2gpMessengerServiceToSendPositiveAcknowledgement() throws Exception {
        ehrCompleteHandler.handleMessage(ehrCompleteEvent);
        verify(gp2gpMessengerService).sendEhrCompletePositiveAcknowledgement(ehrCompleteEvent, transferTrackerDbEntry);
    }

    @Test
    void shouldUpdateDbWithEhrTransferStatusWhenEhrRequestSentSuccessfully() throws Exception {
        ehrCompleteHandler.handleMessage(ehrCompleteEvent);
        verify(transferTrackerService).updateStateOfEhrTransfer(conversationId.toString(),"ACTION:EHR_TRANSFER_TO_REPO_COMPLETE");
    }

    @Test
    void shouldThrowErrorAndNotUpdateDbWhenFailsToSendPositiveAcknowledgement() throws Exception {
        doThrow(Exception.class).when(gp2gpMessengerService).sendEhrCompletePositiveAcknowledgement(ehrCompleteEvent, transferTrackerDbEntry);

        assertThrows(Exception.class, () -> ehrCompleteHandler.handleMessage(ehrCompleteEvent));
        verify(transferTrackerService, never()).updateStateOfEhrTransfer(conversationId.toString(),"ACTION:EHR_TRANSFER_TO_REPO_COMPLETE");
    }
}