package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpmessagemodels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpmessagemodels.SOAPEnvelope;
import uk.nhs.prm.deductions.gp2gpmessagehandler.handlers.CopcMessageHandler;
import uk.nhs.prm.deductions.gp2gpmessagehandler.handlers.EhrExtractMessageHandler;
import uk.nhs.prm.deductions.gp2gpmessagehandler.handlers.EhrRequestMessageHandler;
import uk.nhs.prm.deductions.gp2gpmessagehandler.handlers.MessageHandler;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.ParserService;

import javax.jms.JMSException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@Tag("unit")
public class JmsConsumerTest {
    JmsProducer jmsProducer = mock(JmsProducer.class);
    MessageSanitizer messageSanitizer = mock(MessageSanitizer.class);
    ParserService parserService = mock(ParserService.class);
    EhrExtractMessageHandler ehrExtractMessageHandler = mock(EhrExtractMessageHandler.class);
    CopcMessageHandler copcMessageHandler = mock(CopcMessageHandler.class);
    EhrRequestMessageHandler ehrRequestMessageHandler = mock(EhrRequestMessageHandler.class);

    List<MessageHandler> handlerList = new ArrayList();
    String messageContent = "test";

    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;
    @Value("${activemq.inboundQueue}")
    String inboundQueue;

    JmsConsumer jmsConsumer = new JmsConsumer(jmsProducer, unhandledQueue, inboundQueue, messageSanitizer, parserService, handlerList);

    private void jmsConsumerTestFactory(String expectedQueue) throws JMSException {
        String message = messageContent;
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        bytesMessage.writeBytes(message.getBytes(StandardCharsets.UTF_8));
        bytesMessage.reset();

        jmsConsumer.onMessage(bytesMessage);
        verify(jmsProducer).sendMessageToQueue(expectedQueue, message);
    }

    @Test
    void shouldHandleRCMR_IN030000UK06MessageInEhrExtractMessageHandler() throws IOException, JMSException {
        handlerList.add(ehrExtractMessageHandler);

        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.reset();

        when(ehrExtractMessageHandler.getInteractionId()).thenReturn("RCMR_IN030000UK06");
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getAction()).thenReturn("RCMR_IN030000UK06");
        when(parserService.parse(Mockito.any())).thenReturn(parsedMessage);

        jmsConsumer.onMessage(message);
        verify(ehrExtractMessageHandler).handleMessage(parsedMessage);
    }


    @Test
    void shouldHandleCOPC_IN000001UK01MessageInCopcMessageHandler() throws IOException, JMSException {
        handlerList.add(copcMessageHandler);
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.reset();

        when(copcMessageHandler.getInteractionId()).thenReturn("COPC_IN000001UK01");
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getAction()).thenReturn("COPC_IN000001UK01");
        when(parserService.parse(Mockito.any())).thenReturn(parsedMessage);

        jmsConsumer.onMessage(message);
        verify(copcMessageHandler).handleMessage(parsedMessage);
    }

    @Test
    void shouldHandleRCMR_IN010000UK05MessageInEhrRequestMessageHandler() throws IOException, JMSException {
        handlerList.add(ehrRequestMessageHandler);
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.reset();

        when(ehrRequestMessageHandler.getInteractionId()).thenReturn("RCMR_IN010000UK05");
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getAction()).thenReturn("RCMR_IN010000UK05");
        when(parserService.parse(Mockito.any())).thenReturn(parsedMessage);

        jmsConsumer.onMessage(message);
        verify(ehrRequestMessageHandler).handleMessage(parsedMessage);
    }


    @ParameterizedTest
    @CsvSource({
            "RCMR_IN0UK0",
            ","
    })
    void shouldPutMessageWithInvalidInteractionIdOnUnhandledQueue(String interactionId) throws IOException, JMSException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parserService.parse(Mockito.any())).thenReturn(parsedMessage);
        when(parsedMessage.getAction()).thenReturn(interactionId);
        when(parsedMessage.getRawMessage()).thenReturn(messageContent);

        jmsConsumerTestFactory(unhandledQueue);
    }

    @Test
    void shouldPutMessageWithoutSoapHeaderOnUnhandledQueue() throws IOException, JMSException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getSoapEnvelope()).thenReturn(new SOAPEnvelope());
        when(parsedMessage.getRawMessage()).thenReturn(messageContent);

        when(parserService.parse(Mockito.any())).thenReturn(parsedMessage);
        jmsConsumerTestFactory(unhandledQueue);
    }

    @Test
    void shouldPutMessageOnUnhandledQueueWhenParsingFails() throws JMSException, IOException {
        IOException expectedError = new IOException("failed to parse message");
        when(parserService.parse(Mockito.any())).thenThrow(expectedError);
        jmsConsumerTestFactory(unhandledQueue);
    }

    @Test
    void shouldPutMessageOnUnhandledQueueWhenSanitizingFails() throws JMSException {
        RuntimeException expectedError = new RuntimeException("failed to sanitize message");
        when(messageSanitizer.sanitize(Mockito.any())).thenThrow(expectedError);
        jmsConsumerTestFactory(unhandledQueue);
    }
}
