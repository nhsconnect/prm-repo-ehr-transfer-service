package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
    JmsProducer jmsProducer = mock(JmsProducer.class);
    GPToRepoClient gpToRepoClient = mock(GPToRepoClient.class);
    EhrRepoService ehrRepoService = mock(EhrRepoService.class);

    @Value("${activemq.outboundQueue}")
    String outboundQueue;
    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;

    EhrExtractMessageHandler ehrExtractMessageHandler = new EhrExtractMessageHandler(jmsProducer, outboundQueue, unhandledQueue, gpToRepoClient, ehrRepoService);

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
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        String message = "test";
        when(parsedMessage.isLargeMessage()).thenReturn(false);
        when(parsedMessage.getRawMessage()).thenReturn(message);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, only()).sendMessageToQueue(outboundQueue, message);
    }

    @Test
    public void shouldNotPutLargeHealthRecordsOnJSQueue() throws JMSException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        String message = "test";
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(parsedMessage.getRawMessage()).thenReturn(message);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, never()).sendMessageToQueue("outboundQueue", message);
    }

    @Test
    public void shouldCallGPToRepoToSendContinueMessageForLargeHealthRecords() throws MalformedURLException, URISyntaxException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getMessageId()).thenReturn(ehrExtractMessageId);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(gpToRepoClient).sendContinueMessage(ehrExtractMessageId, conversationId);
    }

    @Test
    public void shouldPutLargeMessageOnUnhandledQueueWhenGPToRepoCallThrows() throws JMSException, MalformedURLException, URISyntaxException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
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
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.isLargeMessage()).thenReturn(true);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(ehrRepoService).storeMessage(parsedMessage);
    }

    @Test
    public void shouldPutLargeMessageOnUnhandledQueueWhenEhrRepoCallThrows() throws JMSException, HttpException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        String message = "test";
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(parsedMessage.getRawMessage()).thenReturn(message);

        HttpException expectedError = new HttpException();
        doThrow(expectedError).when(ehrRepoService).storeMessage(parsedMessage);

        ehrExtractMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, times(1)).sendMessageToQueue(unhandledQueue, message);
    }
}
