package uk.nhs.prm.repo.ehrtransferservice.listeners;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.LargeEhrMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.handlers.S3PointerMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeEhrMessage;

import javax.jms.JMSException;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LargeEhrMessageListenerTest {

    @Mock
    Tracer tracer;
    @Mock
    S3PointerMessageHandler s3PointerMessageHandler;
    @Mock
    LargeEhrMessageHandler largeEhrMessageHandler;

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
        verify(s3PointerMessageHandler).getLargeMessage(payload);
    }

    @Test
    void shouldCallLargeEhrMessageHandlerWithALargeMessage() throws Exception {
        LargeEhrMessage largeEhrMessage = mock(LargeEhrMessage.class);
        when(s3PointerMessageHandler.getLargeMessage(anyString())).thenReturn(largeEhrMessage);
        largeEhrMessageListener.onMessage(getSqsTextMessage());
        verify(largeEhrMessageHandler).handleMessage(largeEhrMessage);
    }

    private SQSTextMessage getSqsTextMessage() throws JMSException {
        String payload = "payload";
        return spy(new SQSTextMessage(payload));
    }
}
