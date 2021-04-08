package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
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
/* here we list classes that we want to be instantiated in the test */
@SpringBootTest(classes = { EhrExtractMessageHandler.class })
public class EhrExtractMessageHandlerTest {
    @Autowired
    EhrExtractMessageHandler messageHandler;

    @MockBean
    JmsTemplate mockJmsTemplate;
    @MockBean
    GPToRepoClient gpToRepoClient;
    @MockBean
    EhrRepoService ehrRepoService;

    @Value("${activemq.outboundQueue}")
    String outboundQueue;

    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;

    private UUID conversationId;
    private UUID ehrExtractMessageId;

    public EhrExtractMessageHandlerTest() {
        conversationId = UUID.randomUUID();
        ehrExtractMessageId = UUID.randomUUID();
    }

    @Test
    public void shouldReturnCorrectInteractionId() {
        assertThat(messageHandler.getInteractionId(), equalTo("RCMR_IN030000UK06"));
    }

    @Test
    public void shouldPutSmallHealthRecordsOnJSQueue() throws JMSException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        when(parsedMessage.isLargeMessage()).thenReturn(false);
        when(parsedMessage.getBytesMessage()).thenReturn(bytesMessage);

        messageHandler.handleMessage(parsedMessage);
        verify(mockJmsTemplate, only()).convertAndSend(outboundQueue, bytesMessage);
    }

    @Test
    public void shouldNotPutLargeHealthRecordsOnJSQueue() throws JMSException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(parsedMessage.getBytesMessage()).thenReturn(bytesMessage);

        messageHandler.handleMessage(parsedMessage);
        verify(mockJmsTemplate, never()).convertAndSend("outboundQueue", bytesMessage);
    }

    @Test
    public void shouldCallGPToRepoToSendContinueMessageForLargeHealthRecords() throws MalformedURLException, URISyntaxException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getMessageId()).thenReturn(ehrExtractMessageId);

        messageHandler.handleMessage(parsedMessage);
        verify(gpToRepoClient).sendContinueMessage(ehrExtractMessageId, conversationId);
    }

    @Test
    public void shouldPutLargeMessageOnUnhandledQueueWhenGPToRepoCallThrows() throws JMSException, MalformedURLException, URISyntaxException, HttpException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getMessageId()).thenReturn(ehrExtractMessageId);
        when(parsedMessage.getBytesMessage()).thenReturn(bytesMessage);

        RuntimeException expectedError = new RuntimeException("Failed to send continue message");
        doThrow(expectedError).when(gpToRepoClient).sendContinueMessage(ehrExtractMessageId, conversationId);

        messageHandler.handleMessage(parsedMessage);
        verify(mockJmsTemplate, times(1)).convertAndSend(unhandledQueue, bytesMessage);
    }

    @Test
    public void shouldCallEhrRepoToStoreMessageForLargeHealthRecords() throws HttpException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.isLargeMessage()).thenReturn(true);

        messageHandler.handleMessage(parsedMessage);
        verify(ehrRepoService).storeMessage(parsedMessage);
    }

    @Test
    public void shouldPutLargeMessageOnUnhandledQueueWhenEhrRepoCallThrows() throws JMSException, HttpException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(parsedMessage.getBytesMessage()).thenReturn(bytesMessage);

        HttpException expectedError = new HttpException();
        doThrow(expectedError).when(ehrRepoService).storeMessage(parsedMessage);

        messageHandler.handleMessage(parsedMessage);
        verify(mockJmsTemplate, times(1)).convertAndSend(unhandledQueue, bytesMessage);
    }
}
