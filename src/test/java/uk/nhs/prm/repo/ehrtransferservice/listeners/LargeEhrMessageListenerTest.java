package uk.nhs.prm.repo.ehrtransferservice.listeners;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.LargeEhrCoreMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.parsers.LargeSqsMessageParser;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;

import javax.jms.JMSException;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LargeEhrMessageListenerTest {

    @Mock
    Tracer tracer;
    @Mock
    LargeSqsMessageParser largeSqsMessageParser;
    @Mock
    LargeEhrCoreMessageHandler largeEhrCoreMessageHandler;

    @InjectMocks
    LargeEhrMessageListener largeEhrMessageListener;

    @Test
    void shouldParseLargeEhrMessage() throws JMSException {
        SQSTextMessage message = getSqsTextMessage();
        largeEhrMessageListener.onMessage(message);
        verify(tracer).setMDCContextFromSqs(message);
    }

    @Test
    void shouldCallLargeEhrSqsServiceWithTheMessagePayload() throws IOException, JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));
        largeEhrMessageListener.onMessage(message);
        verify(largeSqsMessageParser).getLargeSqsMessage(payload);
    }

    @Test
    void shouldCallLargeEhrMessageHandlerWithALargeMessage() throws Exception {
        LargeSqsMessage largeSqsMessage = mock(LargeSqsMessage.class);
        when(largeSqsMessageParser.getLargeSqsMessage(anyString())).thenReturn(largeSqsMessage);
        largeEhrMessageListener.onMessage(getSqsTextMessage());
        verify(largeEhrCoreMessageHandler).handleMessage(largeSqsMessage);
    }

    private SQSTextMessage getSqsTextMessage() throws JMSException {
        String payload = "payload";
        return spy(new SQSTextMessage(payload));
    }
}
