package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.EhrRepoService;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.HttpException;

import javax.jms.JMSException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@Tag("unit")
public class CopcMessageHandlerTest {
    JmsTemplate jmsTemplate = mock(JmsTemplate.class);
    EhrRepoService ehrRepoService = mock(EhrRepoService.class);

    @Value("${activemq.outboundQueue}")
    String outboundQueue;

    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;

    CopcMessageHandler copcMessageHandler = new CopcMessageHandler(jmsTemplate, ehrRepoService, unhandledQueue);

    @Test
    public void shouldReturnCorrectInteractionId() {
        assertThat(copcMessageHandler.getInteractionId(), equalTo("COPC_IN000001UK01"));
    }

    @Test
    public void shouldCallEhrRepoToStoreMessage() throws HttpException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);

        copcMessageHandler.handleMessage(parsedMessage);
        verify(ehrRepoService).storeMessage(parsedMessage);
    }

    @Test
    public void shouldPutMessageOnUnhandledQueueWhenEhrRepoCallThrows() throws JMSException, HttpException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        when(parsedMessage.getBytesMessage()).thenReturn(bytesMessage);

        HttpException expectedError = new HttpException();
        doThrow(expectedError).when(ehrRepoService).storeMessage(parsedMessage);

        copcMessageHandler.handleMessage(parsedMessage);
        verify(jmsTemplate, times(1)).convertAndSend(unhandledQueue, bytesMessage);
    }
}
