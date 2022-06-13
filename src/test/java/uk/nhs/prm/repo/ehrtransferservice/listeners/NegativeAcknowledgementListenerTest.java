package uk.nhs.prm.repo.ehrtransferservice.listeners;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.handlers.NegativeAcknowledgementHandler;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NegativeAcknowledgementListenerTest {

    @Mock
    NegativeAcknowledgementHandler handler;

    @InjectMocks
    NegativeAcknowledgementListener listener;


    @Test
    void shouldPassMessageToHandlerAndAcknowledgeIt() throws Exception {
        var message = spy(new SQSTextMessage("payload"));

        listener.onMessage(message);

        verify(message, times(1)).acknowledge();
        verify(handler).handleMessage("payload");
    }

}