package uk.nhs.prm.repo.ehrtransferservice.listeners;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.LargeMessageFragmentHandler;
import uk.nhs.prm.repo.ehrtransferservice.parsers.LargeSqsMessageParser;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;

import javax.jms.JMSException;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LargeMessageFragmentsListenerTest {

    @Mock
    Tracer tracer;

    @Mock
    LargeSqsMessageParser largeSqsMessageParser;

    @Mock
    LargeMessageFragmentHandler largeMessageFragmentHandler;


    private static final String payload = "payload";

    @InjectMocks
    LargeMessageFragmentsListener largeMessageFragmentsListener;

    @Test
    void shouldSetTraceIdWhenReceivingLargeMessageFragment() throws JMSException {
        var sqsTextMessage = getSqsTextMessage();
        largeMessageFragmentsListener.onMessage(sqsTextMessage);
        verify(tracer).setMDCContextFromSqs(sqsTextMessage);
    }

    @Test
    void shouldCallLargeEhrSqsServiceWithTheMessagePayload() throws IOException, JMSException {
        largeMessageFragmentsListener.onMessage(getSqsTextMessage());
        verify(largeSqsMessageParser).parse(payload);
    }

    @Test
    void shouldCallLargeMessageFragmentHandlerWithLargeSqsPayload() throws Exception {
        var largeSqsMessageMock = mock(LargeSqsMessage.class);
        when(largeSqsMessageParser.parse(payload)).thenReturn(largeSqsMessageMock);
        largeMessageFragmentsListener.onMessage(getSqsTextMessage());
        verify(largeMessageFragmentHandler).handleMessage(largeSqsMessageMock);
    }

    private SQSTextMessage getSqsTextMessage() throws JMSException {
        return spy(new SQSTextMessage(payload));
    }
}
