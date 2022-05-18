package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.json_models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.EhrCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.MessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmallEhrMessageHandlerTest {

    @Mock
    EhrRepoService ehrRepoService;

    @Mock
    MessagePublisher messagePublisher;

    @Mock
    ParsedMessage parsedMessage;

    @Mock
    private EhrCompleteMessagePublisher ehrCompleteMessagePublisher;

    @InjectMocks
    SmallEhrMessageHandler smallEhrMessageHandler;



    private final static String ehrCompleteTopicArn = "ehrCompleteTopicArn";

    private UUID conversationId;
    private UUID messageId;

    public SmallEhrMessageHandlerTest() {
        conversationId = UUID.randomUUID();
        messageId = UUID.randomUUID();
    }

//    @BeforeEach
//    void setUp() {
//        ehrCompleteMessagePublisher = new EhrCompleteMessagePublisher(messagePublisher, ehrCompleteTopicArn);
//    }

    @Test
    public void shouldCallEhrRepoServiceToStoreMessageForSmallEhr() throws Exception {

        smallEhrMessageHandler.handleMessage(parsedMessage);
        verify(ehrRepoService).storeMessage(parsedMessage);
    }

//    @Test
//    public void shouldStoreSmallEhrMessageInEhrRepo() throws Exception {
//        ParsedMessage parsedMessage = mock(ParsedMessage.class);
//
//        smallEhrMessageHandler.handleMessage(parsedMessage);
//        verify(ehrRepoService).storeMessage(parsedMessage);
//    }

    @Test
    public void shouldPublishSmallEhrMessageToEhrCompleteTopic() {
        var ehrCompleteEvent = new EhrCompleteEvent(conversationId, messageId);
        String messageBody = "{\"conversationId\":\"conversationId\",\"messageId\":\"messageId\"}";
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getMessageId()).thenReturn(messageId);
        smallEhrMessageHandler.handleMessage(parsedMessage);
        verify(ehrCompleteMessagePublisher).sendMessage(ehrCompleteEvent);
    }
}