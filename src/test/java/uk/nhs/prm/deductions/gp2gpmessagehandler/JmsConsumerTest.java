package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.MessageHeader;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.SOAPEnvelope;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.SOAPHeader;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.ParserService;

import javax.jms.JMSException;
import javax.mail.MessagingException;
import java.io.IOException;

import static org.mockito.Mockito.*;

/*
 Unit test for JmsConsumer
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class JmsConsumerTest {
    @Mock
    JmsTemplate mockJmsTemplate;
    @Mock
    MessageSanitizer mockMessageSanitizer;
    @Mock
    ParserService mockParserService;

    private SOAPEnvelope getSoapEnvelope(String interactionId) {
        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.header = new SOAPHeader();
        envelope.header.messageHeader = new MessageHeader();
        envelope.header.messageHeader.action = interactionId;
        return envelope;
    }

    private SOAPEnvelope getSoapEnvelopeWithoutSoapHeader() {
        return new SOAPEnvelope();
    }

    private void jmsConsumerTestFactory(String expectedQueue, SOAPEnvelope soapEnv) throws IOException, JMSException, MessagingException {
        JmsConsumer jmsConsumer = new JmsConsumer(mockJmsTemplate, "mockOutbound", "mockUnhandled", "inboundQueue", mockMessageSanitizer, mockParserService);
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.writeBytes(new byte[10]);
        message.reset();
        Mockito.when(mockParserService.parse(Mockito.any())).thenReturn(new ParsedMessage(soapEnv));

        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate, only()).convertAndSend(expectedQueue, message);
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN010000UK05",
            "RCMR_IN030000UK06",
            "PRPA_IN000202UK01"
    })
    void shouldPutValidMessagesOnJSQueue(String interactionId) throws IOException, JMSException, MessagingException {
        jmsConsumerTestFactory("mockOutbound", getSoapEnvelope(interactionId));
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN0UK0",
            ","
    })
    void shouldPutMessageWithInvalidInteractionIdOnUnhandledQueue(String interactionId) throws IOException, JMSException, MessagingException {
        jmsConsumerTestFactory("mockUnhandled", getSoapEnvelope(interactionId));
    }

    @Test
    void shouldPutMessageWithoutSoapHeaderOnUnhandledQueue() throws IOException, JMSException, MessagingException {
        jmsConsumerTestFactory("mockUnhandled", getSoapEnvelopeWithoutSoapHeader());
    }

    @Test
    void shouldPutMessageOnUnhandledQueueWhenParsingFails() throws JMSException, IOException, MessagingException {
        JmsConsumer jmsConsumer = new JmsConsumer(mockJmsTemplate, "mockOutbound", "mockUnhandled", "inboundQueue", mockMessageSanitizer, mockParserService);
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.writeBytes(new byte[10]);
        message.reset();

        Mockito.when(mockParserService.parse(Mockito.any())).thenThrow(new IOException("failed to parse message"));

        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate, only()).convertAndSend("mockUnhandled", message);
    }

    @Test
    void shouldPutMessageOnUnhandledQueueWhenSanitizingFails() throws JMSException {
        JmsConsumer jmsConsumer = new JmsConsumer(mockJmsTemplate, "mockOutbound", "mockUnhandled", "inboundQueue", mockMessageSanitizer, mockParserService);
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.writeBytes(new byte[10]);
        message.reset();

        Mockito.when(mockMessageSanitizer.sanitize((byte[]) Mockito.any())).thenThrow(new RuntimeException("failed to sanitize message"));

        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate, only()).convertAndSend("mockUnhandled", message);
    }
}
