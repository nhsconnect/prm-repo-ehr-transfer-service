package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeMessageFragments;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LargeMessageFragmentHandlerTest {
    @Mock
    EhrRepoService ehrRepoService;

    @Mock
    TransferTrackerService transferTrackerService;

    @InjectMocks
    LargeMessageFragmentHandler largeMessageFragmentHandler;

    @Captor
    ArgumentCaptor<LargeMessageFragments> largeMessageFragmentsArgumentCaptor;

    @Test
    void shouldCallEhrRepoServiceToStoreTheMessage() throws Exception {
        var largeSqsMessage = mock(LargeSqsMessage.class);

        largeMessageFragmentHandler.handleMessage(largeSqsMessage);
        verify(ehrRepoService).storeMessage(largeMessageFragmentsArgumentCaptor.capture());
    }

    @Disabled
    @Test
    void shouldCallTransferTrackerDBServiceWithTheConversationId() throws Exception {
        var mockLargeSqsMessage = mock(LargeSqsMessage.class);
        when(mockLargeSqsMessage.getConversationId()).thenReturn(UUID.randomUUID());
        largeMessageFragmentHandler.handleMessage(mockLargeSqsMessage);
        verify(transferTrackerService).getEhrTransferData(mockLargeSqsMessage.getConversationId().toString());
    }
}