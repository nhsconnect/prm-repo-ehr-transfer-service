package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Value;

import uk.nhs.prm.deductions.gp2gpmessagehandler.services.ParserService;
import uk.nhs.prm.deductions.gp2gpmessagehandler.utils.TestDataLoader;

import javax.jms.JMSException;

import java.io.IOException;

import static org.mockito.Mockito.*;

/*
 Tests JMS Consumer together with queues
 */
@Tag("unit")
public class JmsConsumerIntegrationTest {
    JmsProducer jmsProducer = mock(JmsProducer.class);
    MessageSanitizer messageSanitizer = mock(MessageSanitizer.class);
    ParserService parserService = mock(ParserService.class);

    @Value("${activemq.outboundQueue}")
    String outboundQueue;
    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;
    @Value("${activemq.inboundQueue}")
    String inboundQueue;

    JmsConsumer jmsConsumer = new JmsConsumer(jmsProducer, unhandledQueue, inboundQueue, messageSanitizer, parserService, null);

    private TestDataLoader dataLoader = new TestDataLoader();

    private ActiveMQBytesMessage getActiveMQBytesMessage(byte[] bytes) throws JMSException {
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.writeBytes(bytes);
        message.reset();
        return message;
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "simpleTextMessage.txt",
            "nonSoapMimeMessage.xml",
            "ehrRequestWithoutInteractionId.xml",
            "ehrRequestWithoutMessageHeader.xml",
            "ehrRequestWithoutSoapHeader.xml",
            "ehrRequestIncorrectInteractionId.xml"
    })
    void shouldSendMessageToUnhandledQueue(String fileName) throws JMSException, IOException {
        byte[] bytes = dataLoader.getDataAsBytes(fileName);
        String message = dataLoader.getDataAsString(fileName);

        ActiveMQBytesMessage bytesMessage = getActiveMQBytesMessage(bytes);
        jmsConsumer.onMessage(bytesMessage);
        verify(jmsProducer, times(1)).sendMessageToQueue(ArgumentMatchers.eq(unhandledQueue), ArgumentMatchers.eq(message));
    }
}
