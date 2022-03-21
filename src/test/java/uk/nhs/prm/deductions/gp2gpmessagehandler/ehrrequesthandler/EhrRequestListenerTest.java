package uk.nhs.prm.deductions.gp2gpmessagehandler.ehrrequesthandler;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.deductions.gp2gpmessagehandler.config.Tracer;

import javax.jms.JMSException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EhrRequestListenerTest {

    @Mock
    Tracer tracer;
    @Mock
    EhrRequestService ehrRequestService;
    @Mock
    ConversationIdGenerator conversationIdGenerator;
    @InjectMocks
    EhrRequestListener ehrRequestListener;

    @Test
    void shouldCallTracerWithMessageAndConversation() throws JMSException {
        when(conversationIdGenerator.getConversationId()).thenReturn("unique-uuid");
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));
        ehrRequestListener.onMessage(message);
        verify(tracer).setMDCContext(message, "unique-uuid");
    }

    @Test
    void shouldAcknowledgeTheMessageWhenNoErrors() throws JMSException {
        when(conversationIdGenerator.getConversationId()).thenReturn("unique-uuid");
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));
        ehrRequestListener.onMessage(message);
        verify(message).acknowledge();
    }

    @Test
    void shouldThrowAnExceptionWhenAnyError() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));
        when(message.getText()).thenThrow(new RuntimeException());
        ehrRequestListener.onMessage(message);
        verify(message,never()).acknowledge();
    }
    @Test
    void shouldCallEhrRequestServiceWithTheMessage() throws JMSException {
        String payload = "payload";
        SQSTextMessage message = spy(new SQSTextMessage(payload));
        ehrRequestListener.onMessage(message);
        verify(ehrRequestService).processIncomingEvent(payload);
    }
}