package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

import javax.jms.JMSException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@Tag("unit")
public class PdsUpdateCompletedMessageHandlerTest {
    JmsProducer jmsProducer = mock(JmsProducer.class);

    @Value("${activemq.outboundQueue}")
    String outboundQueue;

    PdsUpdateCompletedMessageHandler pdsUpdateCompletedMessageHandler = new PdsUpdateCompletedMessageHandler(jmsProducer, outboundQueue);

    @Test
    public void shouldReturnCorrectInteractionId() {
        assertThat(pdsUpdateCompletedMessageHandler.getInteractionId(), equalTo("PRPA_IN000202UK01"));
    }

    @Test
    public void shouldPutPdsUpdatedMessagesOnJSQueue() throws JMSException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getRawMessage()).thenReturn("test");

        pdsUpdateCompletedMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, times(1)).sendMessageToQueue(outboundQueue, parsedMessage.getRawMessage());
    }
}
