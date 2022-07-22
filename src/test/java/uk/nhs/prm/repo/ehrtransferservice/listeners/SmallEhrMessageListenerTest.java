package uk.nhs.prm.repo.ehrtransferservice.listeners;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.handlers.SmallEhrMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.parsers.Parser;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmallEhrMessageListenerTest {

    @Mock
    Tracer tracer;

    @Mock
    Parser parser;

    @Mock
    SmallEhrMessageHandler smallEhrMessageHandler;

    @InjectMocks
    SmallEhrMessageListener smallEhrMessageListener;

    public SmallEhrMessageListenerTest() {
        parser = new Parser();
    }

    @Test
    void shouldPassParsedMessageToHandlerAndAcknowledgeIt() throws Exception {
        var message = spy(new SQSTextMessage("payload"));
        var parsedMessage = new StubParsedMessage();
        when(parser.parse("payload")).thenReturn(parsedMessage);

        smallEhrMessageListener.onMessage(message);

        verify(parser).parse("payload");
        verify(tracer).setMDCContextFromSqs(message);
        verify(message, times(1)).acknowledge();
        verify(smallEhrMessageHandler).handleMessage(parsedMessage);
    }

    @Test
    void shouldNotAcknowledgeMessageWhenAnExceptionOccursInParsing() throws Exception {
        var message = spy(new SQSTextMessage("bleuch"));

        when(parser.parse(anyString())).thenThrow(new IllegalArgumentException());

        smallEhrMessageListener.onMessage(message);

        verify(message, never()).acknowledge();
    }

    @Test
    void shouldNotAcknowledgeMessageWhenAnExceptionOccursInHandling() throws Exception {
        var message = spy(new SQSTextMessage("boom"));

        doThrow(new IllegalArgumentException()).when(smallEhrMessageHandler).handleMessage(any());

        smallEhrMessageListener.onMessage(message);

        verify(message, never()).acknowledge();
    }

    class StubParsedMessage extends ParsedMessage {
        public StubParsedMessage() {
            super(null, null, null);
        }
    }
}