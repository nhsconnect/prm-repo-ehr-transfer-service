package uk.nhs.prm.repo.ehrtransferservice;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;
import uk.nhs.prm.repo.ehrtransferservice.handlers.MessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.ParsingDlqPublisher;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Broker;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.MessageSanitizer;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Parser;

import javax.jms.JMSException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@Tag("unit")
public class JmsConsumerTest {
    JmsProducer jmsProducer = mock(JmsProducer.class);
    MessageSanitizer messageSanitizer = mock(MessageSanitizer.class);
    Parser parser = mock(Parser.class);
    ParsingDlqPublisher parsingDlqPublisher = mock(ParsingDlqPublisher.class);
    Broker broker = mock(Broker.class);
    Tracer tracer = mock(Tracer.class);

    List<MessageHandler> handlerList = new ArrayList();
    String messageContent = "test";

    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;
    @Value("${activemq.inboundQueue}")
    String inboundQueue;

    JmsConsumer jmsConsumer = new JmsConsumer(jmsProducer, unhandledQueue, inboundQueue, messageSanitizer, parser, broker, tracer, parsingDlqPublisher, handlerList);

    private void jmsConsumerTestFactory() throws JMSException {
        String message = messageContent;
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        bytesMessage.writeBytes(message.getBytes(StandardCharsets.UTF_8));
        bytesMessage.reset();

        jmsConsumer.onMessage(bytesMessage);
        verify(parsingDlqPublisher).sendMessage(message);
    }

    @Test
    void shouldParseAndCallBrokerForReceivedByteMessage() throws IOException, JMSException {
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.reset();
        var conversationId = UUID.randomUUID();
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getInteractionId()).thenReturn("RCMR_IN030000UK06");
        when(parsedMessage.getRawMessage()).thenReturn("test-message");
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parser.parse(Mockito.any())).thenReturn(parsedMessage);

        jmsConsumer.onMessage(message);
        verify(broker).sendMessageToCorrespondingTopicPublisher(parsedMessage);
    }

    @Test
    void shouldSetTraceId() throws JMSException, IOException {
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.reset();
        var conversationId = UUID.randomUUID();
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getInteractionId()).thenReturn("RCMR_IN030000UK06");
        when(parsedMessage.getRawMessage()).thenReturn("test-message");
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parser.parse(Mockito.any())).thenReturn(parsedMessage);

        jmsConsumer.onMessage(message);
        verify(tracer).setMDCContextFromMhsInbound();
    }

    @Test
    void shouldParseAndCallBrokerForReceivedTextMessage() throws IOException, JMSException {
        var message = new ActiveMQTextMessage();
        message.setText("test-message");
        var conversationId = UUID.randomUUID();
        var parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getInteractionId()).thenReturn("RCMR_IN030000UK06");
        when(parsedMessage.getRawMessage()).thenReturn("test-message");
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parser.parse(Mockito.any())).thenReturn(parsedMessage);

        jmsConsumer.onMessage(message);
        verify(broker).sendMessageToCorrespondingTopicPublisher(parsedMessage);
    }


    @ParameterizedTest
    @CsvSource({
            "RCMR_IN0UK0",
            ","
    })
    void shouldPutMessageWithInvalidInteractionIdOnUnhandledQueue(String interactionId) throws IOException, JMSException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parser.parse(Mockito.any())).thenReturn(parsedMessage);
        when(parsedMessage.getInteractionId()).thenReturn(interactionId);
        when(parsedMessage.getRawMessage()).thenReturn(messageContent);

        jmsConsumerTestFactory();
    }

    @Test
    void shouldPutMessageWithoutSoapHeaderOnUnhandledQueue() throws IOException, JMSException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getSoapEnvelope()).thenReturn(new SOAPEnvelope());
        when(parsedMessage.getRawMessage()).thenReturn(messageContent);

        when(parser.parse(Mockito.any())).thenReturn(parsedMessage);
        jmsConsumerTestFactory();
    }

    @Test
    void shouldPutMessageOnUnhandledQueueWhenParsingFails() throws JMSException, IOException {
        IOException expectedError = new IOException("failed to parse message");
        when(parser.parse(Mockito.any())).thenThrow(expectedError);
        jmsConsumerTestFactory();
    }

    @Test
    void shouldPutMessageOnUnhandledQueueWhenSanitizingFails() throws JMSException {
        RuntimeException expectedError = new RuntimeException("failed to sanitize message");
        when(messageSanitizer.sanitize(Mockito.any())).thenThrow(expectedError);
        jmsConsumerTestFactory();
    }
}
