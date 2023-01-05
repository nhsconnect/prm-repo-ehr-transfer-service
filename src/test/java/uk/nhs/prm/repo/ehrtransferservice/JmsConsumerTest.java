package uk.nhs.prm.repo.ehrtransferservice;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.ParsingDlqPublisher;
import uk.nhs.prm.repo.ehrtransferservice.parsers.AmqpMessageParser;
import uk.nhs.prm.repo.ehrtransferservice.services.Broker;
import uk.nhs.prm.repo.ehrtransferservice.parsers.Parser;

import javax.jms.JMSException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class JmsConsumerTest {
    @Mock
    AmqpMessageParser amqpMessageParser;
    @Mock
    JmsProducer jmsProducer;
    @Mock
    Parser parser;
    @Mock
    ParsingDlqPublisher parsingDlqPublisher;
    @Mock
    Broker broker;
    @Mock
    Tracer tracer;
    @InjectMocks
    JmsConsumer jmsConsumer;

    String messageContent = "test";

    private void verifySentToParsingDlq(String message) throws JMSException {
        verifySentToParsingDlq(message, message);
    }

    private void verifySentToParsingDlq(String message, String expected) throws JMSException {
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        bytesMessage.writeBytes(message.getBytes(StandardCharsets.UTF_8));
        bytesMessage.reset();

        var headerMap = new HashMap<String, Object>();
        headerMap.put("correlation-id", "conversationId");
        jmsConsumer.onMessage(bytesMessage, headerMap);

        verify(parsingDlqPublisher).sendMessage(expected);
    }

    @Test
    void shouldParseAndCallBrokerForReceivedByteMessage() throws IOException, JMSException {
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.reset();
        var conversationId = UUID.randomUUID();
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getInteractionId()).thenReturn("RCMR_IN030000UK06");
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parser.parse(Mockito.any())).thenReturn(parsedMessage);

        var headerMap = new HashMap<String, Object>();
        headerMap.put("correlation-id", conversationId);
        jmsConsumer.onMessage(message, headerMap);

        verify(broker).sendMessageToCorrespondingTopicPublisher(parsedMessage);
    }

    @Test
    void shouldSetTraceIdAndConversationIdWhenParsed() throws JMSException, IOException {
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.reset();
        var conversationId = UUID.randomUUID();
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getInteractionId()).thenReturn("RCMR_IN030000UK06");
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parser.parse(Mockito.any())).thenReturn(parsedMessage);

        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("correlation-id", conversationId);
        jmsConsumer.onMessage(message, headerMap);

        verify(tracer).setMDCContextFromMhsInbound(conversationId.toString());
        verify(tracer).handleConversationId(conversationId.toString());
    }

    @Test
    void shouldPutMessageWithNoInteractionIdOnDLQ() throws IOException, JMSException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(amqpMessageParser.parse(any())).thenReturn(messageContent);
        when(parser.parse(any())).thenReturn(parsedMessage);
        when(parsedMessage.getConversationId()).thenReturn(UUID.randomUUID());
        when(parsedMessage.getInteractionId()).thenReturn(null);

        verifySentToParsingDlq(messageContent);
    }

    @Test
    void shouldPutMessageWithEmptyInteractionIdOnDLQ() throws IOException, JMSException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(amqpMessageParser.parse(any())).thenReturn(messageContent);
        when(parser.parse(any())).thenReturn(parsedMessage);
        when(parsedMessage.getConversationId()).thenReturn(UUID.randomUUID());
        when(parsedMessage.getInteractionId()).thenReturn("   ");

        verifySentToParsingDlq(messageContent);
    }

    @Test
    void shouldPutMessageWithoutSoapHeaderOnDLQ() throws IOException, JMSException {
        when(amqpMessageParser.parse(any())).thenReturn(messageContent);
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parser.parse(any())).thenReturn(parsedMessage);

        verifySentToParsingDlq(messageContent);
    }

    @Test
    void shouldPutMessageOnUnhandledQueueWhenMessageBodyParseFails() throws JMSException {
        var expectedError = new JMSException("failed to parse message");
        when(amqpMessageParser.parse(any())).thenThrow(expectedError);
        verifySentToParsingDlq(messageContent, "NO_ACTION:UNPROCESSABLE_MESSAGE_BODY");
    }

    @Test
    void shouldPutMessageOnUnhandledQueueWhenParsingFails() throws IOException, JMSException {
        IOException expectedError = new IOException("failed to parse message");
        when(amqpMessageParser.parse(any())).thenReturn(messageContent);
        when(parser.parse(Mockito.any())).thenThrow(expectedError);
        verifySentToParsingDlq(messageContent);
    }
}
