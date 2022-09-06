package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;

import javax.jms.JMSException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepoIncomingEventListenerTest {

    @Mock
    Tracer tracer;
    @Mock
    RepoIncomingService repoIncomingService;
    @Mock
    RepoIncomingEventParser incomingEventParser;
    @InjectMocks
    RepoIncomingEventListener repoIncomingEventListener;

    @Test
    void shouldCallTracerWithMessageAndConversation() throws Exception {
        var payload = "payload";
        var incomingEvent = getIncomingEvent();
        when(incomingEventParser.parse(payload)).thenReturn(incomingEvent);

        var message = spy(new SQSTextMessage(payload));
        repoIncomingEventListener.onMessage(message);
        verify(incomingEventParser).parse(payload);
        verify(tracer).setMDCContextFromSqs(message);
        verify(repoIncomingService).processIncomingEvent(incomingEvent);
    }

    private RepoIncomingEvent getIncomingEvent() {
        return new RepoIncomingEvent("111111111", "source-gp", "nem-message-id", "destination-gp", "last-updated", "unique-uuid");
    }

    @Test
    void shouldAcknowledgeTheMessageWhenNoErrors() throws JMSException {
        var payload = "payload";
        var incomingEvent = getIncomingEvent();
        when(incomingEventParser.parse(payload)).thenReturn(incomingEvent);
        var message = spy(new SQSTextMessage(payload));

        repoIncomingEventListener.onMessage(message);

        verify(message).acknowledge();
    }

    @Test
    void shouldThrowAnExceptionWhenAnyError() throws JMSException {
        var payload = "payload";
        var message = spy(new SQSTextMessage(payload));
        when(message.getText()).thenThrow(new RuntimeException());
        repoIncomingEventListener.onMessage(message);
        verify(message, never()).acknowledge();
    }
}