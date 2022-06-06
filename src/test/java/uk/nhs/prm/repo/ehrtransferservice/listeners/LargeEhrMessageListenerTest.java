package uk.nhs.prm.repo.ehrtransferservice.listeners;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.S3PointerMessageHandler;

import javax.jms.JMSException;
import java.io.IOException;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LargeEhrMessageListenerTest {

    @Mock
    Tracer tracer;
    @Mock
    S3PointerMessageHandler s3PointerMessageHandler;

    @InjectMocks
    LargeEhrMessageListener largeEhrMessageListener;

    @Test
    void shouldParseLargeEhrMessage() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));
        largeEhrMessageListener.onMessage(message);
        verify(tracer).setMDCContext(message);
    }

    @Test
    void shouldCallLargeEhrSqsServiceWithTheMessagePayload() throws IOException, JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));
        largeEhrMessageListener.onMessage(message);
        verify(s3PointerMessageHandler).getLargeMessage(payload);
    }
}