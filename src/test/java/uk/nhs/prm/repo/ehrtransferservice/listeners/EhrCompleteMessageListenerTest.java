package uk.nhs.prm.repo.ehrtransferservice.listeners;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.EhrCompleteHandler;
import uk.nhs.prm.repo.ehrtransferservice.json_models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.EhrCompleteParser;

import javax.jms.JMSException;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EhrCompleteMessageListenerTest {
    @Mock
    Tracer tracer;

    @Mock
    EhrCompleteParser ehrCompleteParser;

    @Mock
    EhrCompleteHandler ehrCompleteHandler;

    @InjectMocks
    EhrCompleteMessageListener EhrCompleteMessageListener;

    @Test
    void shouldStartTracingWhenReceivesAMessage() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));

        EhrCompleteMessageListener.onMessage(message);
        verify(tracer).setMDCContext(message);
    }

    @Test
    void shouldParseMessageFromQueue() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));

        EhrCompleteMessageListener.onMessage(message);
        verify(ehrCompleteParser).parse(payload);
    }

    @Test
    void shouldHandleEachMessageFromQueueInEhrCompleteHandler() throws Exception {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));
        var ehrCompleteEvent = new EhrCompleteEvent(UUID.randomUUID(), UUID.randomUUID());
        when(ehrCompleteParser.parse(payload)).thenReturn(ehrCompleteEvent);

        EhrCompleteMessageListener.onMessage(message);
        verify(ehrCompleteHandler).handleMessage(ehrCompleteEvent);
    }
}