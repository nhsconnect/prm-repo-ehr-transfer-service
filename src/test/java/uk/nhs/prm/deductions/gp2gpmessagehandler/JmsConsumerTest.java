package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import uk.nhs.prm.deductions.gp2gpmessagehandler.utils.TestDataLoader;

import javax.jms.JMSException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class JmsConsumerTest {

    @Mock
    JmsTemplate mockJmsTemplate;

    private TestDataLoader dataLoader = new TestDataLoader();

    private ActiveMQBytesMessage getActiveMQBytesMessage(String content) throws JMSException {
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.writeBytes(content.getBytes(StandardCharsets.UTF_8));
        message.reset();
        return message;
    }

    @Test
    private void jmsConsumerTestFactory(String fileName, String expectedQueue) throws IOException, JMSException {
        String ehrRequest = dataLoader.getData(fileName);
        JmsConsumer jmsConsumer = new JmsConsumer(mockJmsTemplate, "outbound", "unhandled");
        ActiveMQBytesMessage message = getActiveMQBytesMessage(ehrRequest);
        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate, only()).convertAndSend(expectedQueue, message);
    }

    @Test
    void shouldSendMessageToOutboundQueue() throws JMSException, IOException {
        jmsConsumerTestFactory("ehrRequestSoapEnvelope.xml", "outbound");
    }

    @Test
    void shouldSendMessageToUnhandledQueue() throws JMSException, IOException {
        jmsConsumerTestFactory("simpleTextMessage.txt", "unhandled");
    }

    @Test
    void shouldSendNonSOAPMessageToUnhandledQueue() throws JMSException, IOException {
        jmsConsumerTestFactory("nonSoapMimeMessage.xml", "unhandled");
    }

    @Test
    void shouldSendMessageWithoutInteractionIdToUnhandledQueue() throws JMSException, IOException {
        jmsConsumerTestFactory("ehrRequestWithoutInteractionId.xml", "unhandled");
    }

    @Test
    void shouldSendMessageWithoutMessageHeaderToUnhandledQueue() throws JMSException, IOException {
        jmsConsumerTestFactory("ehrRequestWithoutMessageHeader.xml", "unhandled");
    }

    @Test
    void shouldSendMessageWithoutSoapHeaderToUnhandledQueue() throws JMSException, IOException {
        jmsConsumerTestFactory("ehrRequestWithoutSoapHeader.xml", "unhandled");
    }
}
