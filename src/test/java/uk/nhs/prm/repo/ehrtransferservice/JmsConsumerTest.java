package uk.nhs.prm.repo.ehrtransferservice;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;
import uk.nhs.prm.repo.ehrtransferservice.handlers.CopcMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.handlers.EhrExtractMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.handlers.EhrRequestMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.handlers.MessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Parser;

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
    Parser parser = mock(Parser.class);
    EhrExtractMessageHandler ehrExtractMessageHandler = mock(EhrExtractMessageHandler.class);
    CopcMessageHandler copcMessageHandler = mock(CopcMessageHandler.class);
    EhrRequestMessageHandler ehrRequestMessageHandler = mock(EhrRequestMessageHandler.class);

    List<MessageHandler> handlerList = new ArrayList();
    String messageContent = "test";

    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;
    @Value("${activemq.inboundQueue}")
    String inboundQueue;

    JmsConsumer jmsConsumer = new JmsConsumer(jmsProducer, unhandledQueue, inboundQueue, messageSanitizer, parser, handlerList);

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
        when(parser.parse(Mockito.any())).thenReturn(parsedMessage);

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
        when(parser.parse(Mockito.any())).thenReturn(parsedMessage);

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
        when(parser.parse(Mockito.any())).thenReturn(parsedMessage);

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
        when(parser.parse(Mockito.any())).thenReturn(parsedMessage);
        when(parsedMessage.getAction()).thenReturn(interactionId);
        when(parsedMessage.getRawMessage()).thenReturn(messageContent);

        jmsConsumerTestFactory(unhandledQueue);
    }

    @Test
    void shouldPutMessageWithoutSoapHeaderOnUnhandledQueue() throws IOException, JMSException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getSoapEnvelope()).thenReturn(new SOAPEnvelope());
        when(parsedMessage.getRawMessage()).thenReturn(messageContent);

        when(parser.parse(Mockito.any())).thenReturn(parsedMessage);
        jmsConsumerTestFactory(unhandledQueue);
    }

    @Test
    void shouldPutMessageOnUnhandledQueueWhenParsingFails() throws JMSException, IOException {
        IOException expectedError = new IOException("failed to parse message");
        when(parser.parse(Mockito.any())).thenThrow(expectedError);
        jmsConsumerTestFactory(unhandledQueue);
    }

    @Test
    void shouldPutMessageOnUnhandledQueueWhenSanitizingFails() throws JMSException {
        RuntimeException expectedError = new RuntimeException("failed to sanitize message");
        when(messageSanitizer.sanitize(Mockito.any())).thenThrow(expectedError);
        jmsConsumerTestFactory(unhandledQueue);
    }
}
