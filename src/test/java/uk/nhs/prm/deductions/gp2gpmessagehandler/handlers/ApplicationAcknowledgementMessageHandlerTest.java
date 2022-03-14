package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.MessageHeader;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.SOAPEnvelope;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.SOAPHeader;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Tag("unit")
public class ApplicationAcknowledgementMessageHandlerTest {
    @Mock
    JmsProducer jmsProducer;

    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;

    private AutoCloseable closeable;

    @InjectMocks
    ApplicationAcknowledgementMessageHandler applicationAcknowledgementMessageHandler;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    public ParsedMessage createParsedMessage() {
        SOAPEnvelope soapEnvelope = new SOAPEnvelope();
        soapEnvelope.header = new SOAPHeader();
        soapEnvelope.header.messageHeader = new MessageHeader();
        soapEnvelope.header.messageHeader.conversationId = UUID.fromString("614DB042-A2B0-4A2A-A0DC-E12C62E13C9F");
        return new ParsedMessage(soapEnvelope, null, null);
    }

    @Test
    public void shouldReturnCorrectInteractionId() {
        assertThat(applicationAcknowledgementMessageHandler.getInteractionId(), equalTo("MCCI_IN010000UK13"));
    }

    @Test
    public void shouldSendToUnhandledQueue()  {
        ParsedMessage parsedMessage = createParsedMessage();
        applicationAcknowledgementMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, times(1)).sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
    }
}