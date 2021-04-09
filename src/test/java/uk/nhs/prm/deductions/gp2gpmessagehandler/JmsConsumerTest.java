package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.SOAPEnvelope;
import uk.nhs.prm.deductions.gp2gpmessagehandler.handlers.EhrExtractMessageHandler;
import uk.nhs.prm.deductions.gp2gpmessagehandler.handlers.MessageHandler;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.ParserService;

import javax.jms.JMSException;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@Tag("unit")
public class JmsConsumerTest {
    JmsTemplate jmsTemplate = mock(JmsTemplate.class);
    MessageSanitizer messageSanitizer = mock(MessageSanitizer.class);
    ParserService parserService = mock(ParserService.class);
    EhrExtractMessageHandler ehrExtractMessageHandler = mock(EhrExtractMessageHandler.class);
    List<MessageHandler> handlerList = new ArrayList();

    JmsConsumerTest(){
        handlerList.add(ehrExtractMessageHandler);
    }

    @Value("${activemq.outboundQueue}")
    String outboundQueue;
    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;
    @Value("${activemq.inboundQueue}")
    String inboundQueue;

    JmsConsumer jmsConsumer = new JmsConsumer(jmsTemplate, unhandledQueue, inboundQueue, messageSanitizer, parserService, handlerList);

    private void jmsConsumerTestFactory(String expectedQueue) throws JMSException {
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.reset();

        jmsConsumer.onMessage(message);
        verify(jmsTemplate, only()).convertAndSend(expectedQueue, message);
    }

    @Test
    void shouldHandleRCMR_IN030000UK06MessageInEhrExtractMessageHandler() throws IOException, JMSException, MessagingException {
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.reset();

        when(ehrExtractMessageHandler.getInteractionId()).thenReturn("RCMR_IN030000UK06");
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getAction()).thenReturn("RCMR_IN030000UK06");
        when(parserService.parse(Mockito.any(), Mockito.any())).thenReturn(parsedMessage);

        jmsConsumer.onMessage(message);
        verify(ehrExtractMessageHandler).handleMessage(parsedMessage);
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN0UK0",
            ","
    })
    void shouldPutMessageWithInvalidInteractionIdOnUnhandledQueue(String interactionId) throws IOException, JMSException, MessagingException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getAction()).thenReturn(interactionId);
        when(parserService.parse(Mockito.any(), Mockito.any())).thenReturn(parsedMessage);
        jmsConsumerTestFactory(unhandledQueue);
    }

    @Test
    void shouldPutMessageWithoutSoapHeaderOnUnhandledQueue() throws IOException, JMSException, MessagingException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getSoapEnvelope()).thenReturn(new SOAPEnvelope());

        when(parserService.parse(Mockito.any(), Mockito.any())).thenReturn(parsedMessage);
        jmsConsumerTestFactory(unhandledQueue);
    }

    @Test
    void shouldPutMessageOnUnhandledQueueWhenParsingFails() throws JMSException, IOException, MessagingException {
        IOException expectedError = new IOException("failed to parse message");
        when(parserService.parse(Mockito.any(),Mockito.any())).thenThrow(expectedError);
        jmsConsumerTestFactory(unhandledQueue);
    }

    @Test
    void shouldPutMessageOnUnhandledQueueWhenSanitizingFails() throws JMSException {
        RuntimeException expectedError = new RuntimeException("failed to sanitize message");
        when(messageSanitizer.sanitize(Mockito.any())).thenThrow(expectedError);
        jmsConsumerTestFactory(unhandledQueue);
    }
}
