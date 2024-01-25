package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferStore;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.Transfer;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LargeEhrCoreMessageHandlerTest {

    @Mock
    EhrRepoService ehrRepoService;

    @Mock
    LargeSqsMessage largeSqsMessage;

    @Mock
    Gp2gpMessengerService gp2gpMessengerService;

    Transfer transfer;

    @Mock
    TransferStore transferStore;

    @InjectMocks
    LargeEhrCoreMessageHandler largeEhrCoreMessageHandler;

    @BeforeEach
    public void setUp() throws Exception {
        transfer = new Transfer("conversationId", "nhsNumber",
                "sourceGP", "nemsMessageId", "nemsEventLastUpdated",
                "state","createdAt", "lastUpdatedAt", "largeEhrCoreMessageId", true);
        when(largeSqsMessage.getConversationId()).thenReturn(UUID.randomUUID());
        when(largeSqsMessage.getMessageId()).thenReturn(UUID.randomUUID());
    }

    @Test
    void shouldCallEhrRepoServiceToStoreMessageForLargeEhr() throws Exception {
        when(transferStore.findTransfer(largeSqsMessage.getConversationId().toString())).thenReturn(transfer);
        largeEhrCoreMessageHandler.handleMessage(largeSqsMessage);
        verify(ehrRepoService).storeMessage(largeSqsMessage);
    }

    @Test
    void shouldCallGp2GpMessengerServiceToMakeContinueRequest() throws Exception {
        when(transferStore.findTransfer(largeSqsMessage.getConversationId().toString())).thenReturn(transfer);
        largeEhrCoreMessageHandler.handleMessage(largeSqsMessage);
        verify(gp2gpMessengerService).sendContinueMessage(largeSqsMessage, transfer);
    }

    @Test
    void shouldCallTransferTrackerDbToUpdateWithExpectedStatus() throws Exception {
        when(transferStore.findTransfer(largeSqsMessage.getConversationId().toString())).thenReturn(transfer);
        largeEhrCoreMessageHandler.handleMessage(largeSqsMessage);
        verify(transferStore).handleEhrTransferStateUpdate(largeSqsMessage.getConversationId().toString(), "nemsMessageId" ,"ACTION:LARGE_EHR_CONTINUE_REQUEST_SENT", true);
    }

    @Test
    void shouldCallTransferTrackerDbToUpdateWithLargeEhrCoreMessageId() throws Exception {
        when(transferStore.findTransfer(largeSqsMessage.getConversationId().toString())).thenReturn(transfer);
        largeEhrCoreMessageHandler.handleMessage(largeSqsMessage);
        verify(transferStore).updateLargeEhrCoreMessageId(largeSqsMessage.getConversationId().toString(), largeSqsMessage.getMessageId().toString());
    }

}