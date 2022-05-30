package uk.nhs.prm.repo.ehrtransferservice.listeners;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.EhrCompleteParser;

import javax.jms.JMSException;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EhrCompleteMessageListenerTest {
    @Mock
    Tracer tracer;

    @Mock
    EhrCompleteParser ehrCompleteParser;

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
}