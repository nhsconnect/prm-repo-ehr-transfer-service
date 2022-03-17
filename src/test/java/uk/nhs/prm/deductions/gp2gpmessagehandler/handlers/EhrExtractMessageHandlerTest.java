package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpmessagemodels.*;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.EhrRepoService;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.GPToRepoClient;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.HttpException;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@Tag("unit")
public class EhrExtractMessageHandlerTest {
    @Mock
    JmsProducer jmsProducer;
    @Mock
    GPToRepoClient gpToRepoClient;
    @Mock
    EhrRepoService ehrRepoService;
    @Mock
    ParsedMessage parsedMessage;
    private AutoCloseable closeable;

    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;

    @InjectMocks
    EhrExtractMessageHandler ehrExtractMessageHandler;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    private UUID conversationId;
    private UUID messageId;

    public EhrExtractMessageHandlerTest() {
        conversationId = UUID.randomUUID();
        messageId = UUID.randomUUID();
    }

    @Test
    public void shouldReturnCorrectInteractionId() {
        assertThat(ehrExtractMessageHandler.getInteractionId(), equalTo("RCMR_IN030000UK06"));
    }

    @Test
    public void shouldCallEhrRepoToStoreMessageForLargeHealthRecords() throws HttpException {
        when(parsedMessage.isLargeMessage()).thenReturn(true);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(ehrRepoService).storeMessage(parsedMessage);
    }

    @Test
    public void shouldCallGPToRepoToSendContinueMessageForLargeHealthRecords() throws HttpException {
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getMessageId()).thenReturn(messageId);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(gpToRepoClient).sendContinueMessage(messageId, conversationId);
    }

    @Test
    public void shouldPutLargeMessageOnUnhandledQueueWhenGPToRepoCallThrows() throws HttpException {
        String message = "test";
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getMessageId()).thenReturn(messageId);
        when(parsedMessage.getRawMessage()).thenReturn(message);

        RuntimeException expectedError = new RuntimeException("Failed to send continue message");
        doThrow(expectedError).when(gpToRepoClient).sendContinueMessage(messageId, conversationId);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, times(1)).sendMessageToQueue(unhandledQueue, message);
    }

    @Test
    public void shouldPutLargeMessageOnUnhandledQueueWhenEhrRepoCallThrows() throws HttpException {
        String message = "test";
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(parsedMessage.getRawMessage()).thenReturn(message);

        HttpException expectedError = new HttpException();
        doThrow(expectedError).when(ehrRepoService).storeMessage(parsedMessage);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, times(1)).sendMessageToQueue(unhandledQueue, message);
    }

    @Test
    public void shouldCallEhrRepoToStoreMessageForSmallHealthRecords() throws HttpException {
        when(parsedMessage.isLargeMessage()).thenReturn(false);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(ehrRepoService).storeMessage(parsedMessage);
    }

    @Test
    public void shouldCallGPToRepoToSendEhrExtractReceivedNotificationForSmallHealthRecords() throws HttpException {
        when(parsedMessage.isLargeMessage()).thenReturn(false);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getMessageId()).thenReturn(messageId);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(gpToRepoClient).notifySmallEhrExtractArrived(messageId, conversationId);
    }

    @Test
    public void shouldPutSmallMessageOnUnhandledQueueWhenEhrRepoCallThrows() throws HttpException {
        String message = "test";
        when(parsedMessage.isLargeMessage()).thenReturn(false);
        when(parsedMessage.getRawMessage()).thenReturn(message);

        HttpException expectedError = new HttpException();
        doThrow(expectedError).when(ehrRepoService).storeMessage(parsedMessage);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, times(1)).sendMessageToQueue(unhandledQueue, message);
    }

    @Test
    public void shouldPutSmallMessageOnUnhandledQueueWhenGPToRepoCallThrows() throws HttpException {
        String message = "test";
        when(parsedMessage.isLargeMessage()).thenReturn(false);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getMessageId()).thenReturn(messageId);
        when(parsedMessage.getRawMessage()).thenReturn(message);

        HttpException expectedError = new HttpException("Failed to send the small EHR extract received notification");
        doThrow(expectedError).when(gpToRepoClient).notifySmallEhrExtractArrived(messageId, conversationId);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, times(1)).sendMessageToQueue(unhandledQueue, message);
    }
}
