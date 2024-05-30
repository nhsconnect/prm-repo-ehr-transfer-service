package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.acknowledgement.EhrCompleteAcknowledgementFailedException;
import uk.nhs.prm.repo.ehrtransferservice.logging.Tracer;

import javax.jms.JMSException;

import java.util.UUID;

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
        // given
        var payload = "payload";
        var incomingEvent = getIncomingEvent();

        var message = spy(new SQSTextMessage(payload));

        // when
        when(incomingEventParser.parse(payload)).thenReturn(incomingEvent);
        repoIncomingEventListener.onMessage(message);

        // then
        verify(incomingEventParser).parse(payload);
        verify(tracer).setMDCContextFromSqs(message);
        verify(repoIncomingService).processIncomingEvent(incomingEvent);
    }

    @Test
    void shouldAcknowledgeTheMessageWhenNoExceptionThrown() throws JMSException {
        // given
        var payload = "payload";
        var incomingEvent = getIncomingEvent();
        var message = spy(new SQSTextMessage(payload));

        // when
        when(incomingEventParser.parse(payload)).thenReturn(incomingEvent);
        repoIncomingEventListener.onMessage(message);

        // then
        verify(message).acknowledge();
    }

    @Test
    void shouldAcknowledgeTheMessageWhenEhrCompleteAcknowledgementFailedExceptionThrown() throws JMSException {
        // given
        var payload = "payload";
        var message = spy(new SQSTextMessage(payload));

        // when
        when(message.getText()).thenThrow(new EhrCompleteAcknowledgementFailedException(UUID.randomUUID(), new Throwable()));
        repoIncomingEventListener.onMessage(message);

        // then
        verify(message).acknowledge();
    }

    @Test
    void shouldNotAcknowledgeTheMessageWhenAnyOtherExceptionThrown() throws JMSException {
        // given
        var payload = "payload";
        var message = spy(new SQSTextMessage(payload));

        // when
        when(message.getText()).thenThrow(new RuntimeException());
        repoIncomingEventListener.onMessage(message);

        // then
        verify(message, never()).acknowledge();
    }

    private RepoIncomingEvent getIncomingEvent() {
        return new RepoIncomingEvent("111111111", "source-gp", "nem-message-id", "destination-gp", "last-updated", "unique-uuid");
    }
}