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
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.*;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.EhrRepoService;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.GPToRepoClient;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.HttpException;

import javax.jms.JMSException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

    @Value("${activemq.outboundQueue}")
    String outboundQueue;
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
    private UUID ehrExtractMessageId;

    public EhrExtractMessageHandlerTest() {
        conversationId = UUID.randomUUID();
        ehrExtractMessageId = UUID.randomUUID();
    }

    @Test
    public void shouldReturnCorrectInteractionId() {
        assertThat(ehrExtractMessageHandler.getInteractionId(), equalTo("RCMR_IN030000UK06"));
    }

    @Test
    public void shouldPutSmallHealthRecordsOnJSQueue() throws JMSException {
        String message = "test";
        when(parsedMessage.isLargeMessage()).thenReturn(false);
        when(parsedMessage.getRawMessage()).thenReturn(message);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, only()).sendMessageToQueue(outboundQueue, message);
    }

    @Test
    public void shouldNotPutLargeHealthRecordsOnJSQueue() throws JMSException {
        String message = "test";
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(parsedMessage.getRawMessage()).thenReturn(message);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, never()).sendMessageToQueue("outboundQueue", message);
    }

    @Test
    public void shouldCallGPToRepoToSendContinueMessageForLargeHealthRecords() throws MalformedURLException, URISyntaxException, HttpException {
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getMessageId()).thenReturn(ehrExtractMessageId);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(gpToRepoClient).sendContinueMessage(ehrExtractMessageId, conversationId);
    }

    @Test
    public void shouldPutLargeMessageOnUnhandledQueueWhenGPToRepoCallThrows() throws JMSException, MalformedURLException, URISyntaxException, HttpException {
        String message = "test";
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getMessageId()).thenReturn(ehrExtractMessageId);
        when(parsedMessage.getRawMessage()).thenReturn(message);

        RuntimeException expectedError = new RuntimeException("Failed to send continue message");
        doThrow(expectedError).when(gpToRepoClient).sendContinueMessage(ehrExtractMessageId, conversationId);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, times(1)).sendMessageToQueue(unhandledQueue, message);
    }

    @Test
    public void shouldCallEhrRepoToStoreMessageForLargeHealthRecords() throws HttpException {
        when(parsedMessage.isLargeMessage()).thenReturn(true);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(ehrRepoService).storeMessage(parsedMessage);
    }

    @Test
    public void shouldPutLargeMessageOnUnhandledQueueWhenEhrRepoCallThrows() throws JMSException, HttpException {
        String message = "test";
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(parsedMessage.getRawMessage()).thenReturn(message);

        HttpException expectedError = new HttpException();
        doThrow(expectedError).when(ehrRepoService).storeMessage(parsedMessage);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, times(1)).sendMessageToQueue(unhandledQueue, message);
    }
}
