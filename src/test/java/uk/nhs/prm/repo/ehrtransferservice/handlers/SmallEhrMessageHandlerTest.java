package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmallEhrMessageHandlerTest {

    @Mock
    EhrRepoService ehrRepoService;
    @InjectMocks
    SmallEhrMessageHandler smallEhrMessageHandler;

    @Test
   public void shouldStoreSmallEhrMessageInEhrRepo() throws Exception {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);

        smallEhrMessageHandler.handleMessage(parsedMessage);
        verify(ehrRepoService).storeMessage(parsedMessage);
    }
}