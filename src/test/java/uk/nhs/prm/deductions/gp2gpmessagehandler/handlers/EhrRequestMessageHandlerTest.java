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
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

import javax.jms.JMSException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@Tag("unit")
public class EhrRequestMessageHandlerTest {
    @Mock
    JmsProducer jmsProducer;

    @Mock
    ParsedMessage parsedMessage;

    @Value("${activemq.outboundQueue}")
    String outboundQueue;
    private AutoCloseable closeable;

    @InjectMocks
    EhrRequestMessageHandler ehrRequestMessageHandler;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void shouldReturnCorrectInteractionId() {
        assertThat(ehrRequestMessageHandler.getInteractionId(), equalTo("RCMR_IN010000UK05"));
    }

    @Test
    public void shouldPutEhrRequestMessagesOnJSQueue() throws JMSException {
        String message = "test";
        when(parsedMessage.getRawMessage()).thenReturn(message);
        when(parsedMessage.isLargeMessage()).thenReturn(false);

        ehrRequestMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, times(1)).sendMessageToQueue(outboundQueue, message);
    }
}
