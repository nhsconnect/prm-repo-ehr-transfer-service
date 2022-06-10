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
class LargeMessageFragmentsListenerTest {

    @Mock
    Tracer tracer;
    @Mock
    S3PointerMessageHandler s3PointerMessageHandler;

    @InjectMocks
    LargeMessageFragmentsListener largeMessageFragmentsListener;

    @Test
    void shouldSetTraceIdWhenReceivingLargeMessageFragment() throws JMSException {
        SQSTextMessage message = getSqsTextMessage();
        largeMessageFragmentsListener.onMessage(message);

        verify(tracer).setMDCContextFromSqs(message);
    }

    @Test
    void shouldCallLargeEhrSqsServiceWithTheMessagePayload() throws IOException, JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));
        largeMessageFragmentsListener.onMessage(message);

        verify(s3PointerMessageHandler).getLargeSqsMessage(payload);
    }

    private SQSTextMessage getSqsTextMessage() throws JMSException {
        String payload = "payload";
        return spy(new SQSTextMessage(payload));
    }
}
