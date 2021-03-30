package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.EhrRepoService;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.HttpException;

import javax.jms.JMSException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@Tag("unit")
/* here we list classes that we want to be instantiated in the test */
@SpringBootTest(classes = { CopcMessageHandler.class })
public class CopcMessageHandlerTest {
    @Autowired
    CopcMessageHandler messageHandler;

    @MockBean
    JmsTemplate mockJmsTemplate;
    @MockBean
    EhrRepoService ehrRepoService;

    @Value("${activemq.outboundQueue}")
    String outboundQueue;

    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;

    private ActiveMQBytesMessage getActiveMQBytesMessage() throws JMSException {
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        bytesMessage.writeBytes(new byte[10]);
        bytesMessage.reset();
        return bytesMessage;
    }

    @Test
    public void shouldReturnCorrectInteractionId() {
        assertThat(messageHandler.getInteractionId(), equalTo("COPC_IN000001UK01"));
    }

    @Test
    public void shouldCallEhrRepoToStoreMessage() throws JMSException, HttpException {
        ParsedMessage parsedMessage = new ParsedMessage(null, null);
        ActiveMQBytesMessage bytesMessage = getActiveMQBytesMessage();
        byte[] messageAsBytes = new byte[(int) bytesMessage.getBodyLength()];

        messageHandler.handleMessage(parsedMessage, bytesMessage);
        verify(ehrRepoService).storeMessage(parsedMessage, messageAsBytes);
    }

    @Test
    public void shouldPutMessageOnUnhandledQueueWhenEhrRepoCallThrows() throws JMSException, HttpException {
        ParsedMessage parsedMessage = new ParsedMessage(null, null);
        ActiveMQBytesMessage bytesMessage = getActiveMQBytesMessage();
        byte[] messageAsBytes = new byte[(int) bytesMessage.getBodyLength()];

        HttpException expectedError = new HttpException();
        doThrow(expectedError).when(ehrRepoService).storeMessage(parsedMessage, messageAsBytes);

        messageHandler.handleMessage(parsedMessage, bytesMessage);
        verify(mockJmsTemplate, times(1)).convertAndSend(unhandledQueue, bytesMessage);
    }
}
