package uk.nhs.prm.repo.ehrtransferservice.listeners;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.LargeMessageFragmentHandler;
import uk.nhs.prm.repo.ehrtransferservice.parsers.S3PointerMessageFetcher;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LargeMessageFragmentsListenerTest {

    @Mock
    Tracer tracer;

    @Mock
    S3PointerMessageFetcher s3PointerMessageFetcher;

    @Mock
    LargeMessageFragmentHandler largeMessageFragmentHandler;

    private static final String payload = "payload";

    @InjectMocks
    LargeMessageFragmentsListener largeMessageFragmentsListener;

    @Test
    void shouldSetTraceIdWhenReceivingLargeMessageFragment() throws Exception {
        var sqsTextMessage = getSqsTextMessage();
        largeMessageFragmentsListener.onMessage(sqsTextMessage);
        verify(tracer).setMDCContextFromSqs(sqsTextMessage);
    }

    @Test
    void shouldCallLargeEhrSqsServiceWithTheMessagePayload() throws Exception {
        var message = getSqsTextMessage();
        largeMessageFragmentsListener.onMessage(message);
        verify(s3PointerMessageFetcher).parse(message);
    }

    @Test
    void shouldCallLargeMessageFragmentHandlerWithLargeSqsPayload() throws Exception {
        var message = getSqsTextMessage();
        var largeSqsMessageMock = mock(LargeSqsMessage.class);
        when(s3PointerMessageFetcher.parse(message)).thenReturn(largeSqsMessageMock);
        largeMessageFragmentsListener.onMessage(message);
        verify(largeMessageFragmentHandler).handleMessage(largeSqsMessageMock);
    }

    private SQSTextMessage getSqsTextMessage() throws Exception {
        return spy(new SQSTextMessage(payload));
    }
}
