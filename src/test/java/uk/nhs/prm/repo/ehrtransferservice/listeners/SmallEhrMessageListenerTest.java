package uk.nhs.prm.repo.ehrtransferservice.listeners;

import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Parser;

import javax.jms.JMSException;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmallEhrMessageListenerTest {

    @Mock
    Tracer tracer;


    @Mock
    Parser parser;


    @InjectMocks
    SmallEhrMessageListener smallEhrMessageListener;

    public SmallEhrMessageListenerTest() {
        parser = new Parser();
    }


    @Test
    void shouldParseSmallEhrMessage() throws JMSException, IOException {
        String payload = "payload";


        SQSTextMessage message = spy(new SQSTextMessage(payload));

        smallEhrMessageListener.onMessage(message);
        verify(parser).parse(payload);
        verify(tracer).setMDCContext(message);
    }
}