package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LargeMessageFragmentHandlerTest {
    @Mock
    EhrRepoService ehrRepoService;

    @InjectMocks
    LargeMessageFragmentHandler largeMessageFragmentHandler;

    @Test
    void shouldCallEhrRepoServiceToStoreTheMessage() throws Exception {
        var mockLargeSqsMessage = mock(LargeSqsMessage.class);
        largeMessageFragmentHandler.handleMessage(mockLargeSqsMessage);
        verify(ehrRepoService).storeMessage(mockLargeSqsMessage);
    }
}