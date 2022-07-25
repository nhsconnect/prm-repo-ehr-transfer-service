package uk.nhs.prm.repo.ehrtransferservice.listeners;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.LargeEhrCoreMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;
import uk.nhs.prm.repo.ehrtransferservice.parsers.S3PointerMessageFetcher;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LargeEhrMessageListenerTest {

    @Mock
    Tracer tracer;
    @Mock
    S3PointerMessageFetcher s3PointerMessageFetcher;
    @Mock
    LargeEhrCoreMessageHandler largeEhrCoreMessageHandler;

    @InjectMocks
    LargeEhrMessageListener largeEhrMessageListener;

    @Test
    void shouldParseLargeEhrMessage() throws Exception {
        SQSTextMessage message = getSqsTextMessage();
        largeEhrMessageListener.onMessage(message);
        verify(tracer).setMDCContextFromSqs(message);
    }

    @Test
    void shouldCallLargeEhrSqsServiceWithTheMessagePayload() throws Exception {
        var message = spy(new SQSTextMessage("payload"));
        largeEhrMessageListener.onMessage(message);
        verify(s3PointerMessageFetcher).parse(message);
    }

    @Test
    void shouldCallLargeEhrMessageHandlerWithALargeMessage() throws Exception {
        var message = getSqsTextMessage();
        var largeSqsMessage = mock(LargeSqsMessage.class);
        when(s3PointerMessageFetcher.parse(message)).thenReturn(largeSqsMessage);
        largeEhrMessageListener.onMessage(message);
        verify(largeEhrCoreMessageHandler).handleMessage(largeSqsMessage);
    }

    private SQSTextMessage getSqsTextMessage() throws Exception {
        String payload = "payload";
        return spy(new SQSTextMessage(payload));
    }
}
