package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.MessageHeader;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.SOAPEnvelope;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.SOAPHeader;
import uk.nhs.prm.deductions.gp2gpmessagehandler.handlers.EhrExtractMessageHandler;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.ParserService;

import javax.jms.JMSException;
import javax.mail.MessagingException;
import java.io.IOException;

import static org.mockito.Mockito.*;

/*
 Unit test for JmsConsumer
 */
@Tag("unit")
@SpringBootTest(classes = { JmsConsumer.class })
public class JmsConsumerTest {
    @Autowired
    JmsConsumer jmsConsumer;

    @MockBean
    JmsTemplate jmsTemplate;
    @MockBean
    MessageSanitizer messageSanitizer;
    @MockBean
    ParserService parserService;
    @MockBean
    EhrExtractMessageHandler ehrExtractMessageHandler;
    @Value("${activemq.outboundQueue}")
    String outboundQueue;
    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;
    @Value("${activemq.inboundQueue}")
    String inboundQueue;

    private SOAPEnvelope getSoapEnvelope(String interactionId) {
        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.header = new SOAPHeader();
        envelope.header.messageHeader = new MessageHeader();
        envelope.header.messageHeader.action = interactionId;
        return envelope;
    }

    private ActiveMQBytesMessage getActiveMQBytesMessage() throws JMSException {
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.writeBytes(new byte[10]);
        message.reset();
        return message;
    }

    private void jmsConsumerTestFactory(String expectedQueue) throws JMSException {
        ActiveMQBytesMessage message = getActiveMQBytesMessage();

        jmsConsumer.onMessage(message);
        verify(jmsTemplate, only()).convertAndSend(expectedQueue, message);
    }

    @BeforeEach
    void setupTest(){
        when(ehrExtractMessageHandler.getInteractionId()).thenReturn("RCMR_IN030000UK06");
    }

    @Test
    void shouldHandleRCMR_IN030000UK06MessageInEhrExtractMessageHandler() throws IOException, JMSException, MessagingException {
        ActiveMQBytesMessage message = getActiveMQBytesMessage();
        ParsedMessage parsedMessage = new ParsedMessage(getSoapEnvelope("RCMR_IN030000UK06"), null, null);
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
        ParsedMessage parsedMessage = new ParsedMessage(getSoapEnvelope(interactionId), null, null);
        when(parserService.parse(Mockito.any(), Mockito.any())).thenReturn(parsedMessage);
        jmsConsumerTestFactory(unhandledQueue);
    }

    @Test
    void shouldPutMessageWithoutSoapHeaderOnUnhandledQueue() throws IOException, JMSException, MessagingException {
        ParsedMessage parsedMessage = new ParsedMessage(new SOAPEnvelope(), null, null);
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
