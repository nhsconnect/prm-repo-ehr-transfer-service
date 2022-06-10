package uk.nhs.prm.repo.ehrtransferservice;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
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
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Broker;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.MessageSanitizer;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Parser;

import javax.jms.JMSException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class JmsConsumerTest {
    @Mock
    JmsProducer jmsProducer;
    @Mock
    MessageSanitizer messageSanitizer;
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

    private void verifySentToParsingDlq() throws JMSException {
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
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parser.parse(Mockito.any())).thenReturn(parsedMessage);

        jmsConsumer.onMessage(message);
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

        jmsConsumer.onMessage(message);
        verify(tracer).setMDCContextFromMhsInbound(null);
        verify(tracer).handleConversationId(conversationId.toString());
    }

    @Test
    void shouldParseAndCallBrokerForReceivedTextMessage() throws IOException, JMSException {
        var message = new ActiveMQTextMessage();
        message.setText("test-message");
        var conversationId = UUID.randomUUID();
        var parsedMessage = mock(ParsedMessage.class);
        when(parsedMessage.getInteractionId()).thenReturn("RCMR_IN030000UK06");
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parser.parse(Mockito.any())).thenReturn(parsedMessage);

        jmsConsumer.onMessage(message);
        verify(broker).sendMessageToCorrespondingTopicPublisher(parsedMessage);
    }

    @Test
    void shouldPutMessageWithNoInteractionIdOnDLQ() throws JMSException, IOException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parser.parse(any())).thenReturn(parsedMessage);
        when(parsedMessage.getConversationId()).thenReturn(UUID.randomUUID());
        when(parsedMessage.getInteractionId()).thenReturn(null);

        verifySentToParsingDlq();
    }

    @Test
    void shouldPutMessageWithEmptyInteractionIdOnDLQ() throws JMSException, IOException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        when(parser.parse(any())).thenReturn(parsedMessage);
        when(parsedMessage.getConversationId()).thenReturn(UUID.randomUUID());
        when(parsedMessage.getInteractionId()).thenReturn("   ");

        verifySentToParsingDlq();
    }

    @Test
    void shouldPutMessageWithoutSoapHeaderOnDLQ() throws IOException, JMSException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);

        when(parser.parse(any())).thenReturn(parsedMessage);
        verifySentToParsingDlq();
    }

    @Test
    void shouldPutMessageOnUnhandledQueueWhenParsingFails() throws JMSException, IOException {
        IOException expectedError = new IOException("failed to parse message");
        when(parser.parse(Mockito.any())).thenThrow(expectedError);
        verifySentToParsingDlq();
    }

    @Test
    void shouldPutMessageOnUnhandledQueueWhenSanitizingFails() throws JMSException {
        RuntimeException expectedError = new RuntimeException("failed to sanitize message");
        when(messageSanitizer.sanitize(Mockito.any())).thenThrow(expectedError);
        verifySentToParsingDlq();
    }
}
