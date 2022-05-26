package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.json_models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.EhrCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LargeEhrMessageHandlerTest {

    @Mock
    EhrRepoService ehrRepoService;

    @Mock
    ParsedMessage parsedMessage;

    @Mock
    private EhrCompleteMessagePublisher ehrCompleteMessagePublisher;

    @InjectMocks
    LargeEhrMessageHandler largeEhrMessageHandler;


//    private final static String ehrCompleteTopicArn = "ehrCompleteTopicArn";

    private UUID conversationId;
    private UUID messageId;

    public LargeEhrMessageHandlerTest() {
        conversationId = UUID.randomUUID();
        messageId = UUID.randomUUID();
    }


    @Test
    public void shouldCallEhrRepoServiceToStoreMessageForLargeEhr() throws Exception {
        largeEhrMessageHandler.handleMessage(parsedMessage);
        verify(ehrRepoService).storeMessage(parsedMessage);
    }


    @Test
    public void shouldPublishLargeEhrMessageToEhrCompleteTopic() throws Exception {
        var ehrCompleteEvent = new EhrCompleteEvent(conversationId, messageId);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getMessageId()).thenReturn(messageId);
        largeEhrMessageHandler.handleMessage(parsedMessage);
        verify(ehrCompleteMessagePublisher).sendMessage(ehrCompleteEvent);
    }
}