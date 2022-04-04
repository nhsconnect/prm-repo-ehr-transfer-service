package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.services.HttpException;

import javax.jms.JMSException;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepoIncomingEventListenerTest {

    @Mock
    Tracer tracer;
    @Mock
    RepoIncomingService repoIncomingService;
    @Mock
    ConversationIdStore conversationIdStore;
    @Mock
    RepoIncomingEventParser incomingEventParser;
    @InjectMocks
    RepoIncomingEventListener repoIncomingEventListener;

    @Test
    void shouldCallTracerWithMessageAndConversation() throws JMSException, HttpException, IOException, URISyntaxException, InterruptedException {
        when(conversationIdStore.getConversationId()).thenReturn("unique-uuid");
        String payload = "payload";
        RepoIncomingEvent incomingEvent = getIncomingEvent();
        when(incomingEventParser.parse(payload)).thenReturn(incomingEvent);

        SQSTextMessage message = spy(new SQSTextMessage(payload));
        repoIncomingEventListener.onMessage(message);
        verify(incomingEventParser).parse(payload);
        verify(tracer).setMDCContext(message, "unique-uuid");
        verify(repoIncomingService).processIncomingEvent(incomingEvent);
    }

    private RepoIncomingEvent getIncomingEvent() {
        return new RepoIncomingEvent("111111111","source-gp","nem-message-id","destination-gp");
    }

    @Test
    void shouldAcknowledgeTheMessageWhenNoErrors() throws JMSException {
        when(conversationIdStore.getConversationId()).thenReturn("unique-uuid");
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));
        repoIncomingEventListener.onMessage(message);
        verify(message).acknowledge();
    }

    @Test
    void shouldThrowAnExceptionWhenAnyError() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));
        when(message.getText()).thenThrow(new RuntimeException());
        repoIncomingEventListener.onMessage(message);
        verify(message, never()).acknowledge();
    }
}