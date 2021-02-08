package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.activemq.command.ActiveMQBytesMessage;
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
    void shouldSendMessageToOutboundQueue() throws JMSException, IOException {
        String ehrRequest = dataLoader.getData("ehrRequestSoapEnvelope.xml");
        JmsConsumer jmsConsumer = new JmsConsumer(mockJmsTemplate, "outbound", "unhandled");
        ActiveMQBytesMessage message = getActiveMQBytesMessage(ehrRequest);
        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate, only()).convertAndSend("outbound", message);
    }

    @Test
    void shouldSendMessageToUnhandledQueue() throws JMSException {
        JmsConsumer jmsConsumer = new JmsConsumer(mockJmsTemplate, "outbound", "unhandled");
        ActiveMQBytesMessage message = getActiveMQBytesMessage("hello");
        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate, only()).convertAndSend("unhandled", message);
    }

    @Test
    void shouldSendNonSOAPMessageToUnhandledQueue() throws JMSException, IOException {
        String nonSoapMessage = dataLoader.getData("nonSoapMimeMessage.xml");
        JmsConsumer jmsConsumer = new JmsConsumer(mockJmsTemplate, "outbound", "unhandled");
        ActiveMQBytesMessage message = getActiveMQBytesMessage(nonSoapMessage);
        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate, only()).convertAndSend("unhandled", message);
    }

    @Test
    void shouldSendMessageWithoutInteractionIdToUnhandledQueue() throws JMSException, IOException {
        String messageWithoutInteractionId = dataLoader.getData("ehrRequestWithoutInteractionId.xml");
        JmsConsumer jmsConsumer = new JmsConsumer(mockJmsTemplate, "outbound", "unhandled");
        ActiveMQBytesMessage message = getActiveMQBytesMessage(messageWithoutInteractionId);
        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate, only()).convertAndSend("unhandled", message);
    }

    @Test
    void shouldSendMessageWithoutMessageHeaderToUnhandledQueue() throws JMSException, IOException {
        String messageWithoutMessageHeader = dataLoader.getData("ehrRequestWithoutMessageHeader.xml");
        JmsConsumer jmsConsumer = new JmsConsumer(mockJmsTemplate, "outbound", "unhandled");
        ActiveMQBytesMessage message = getActiveMQBytesMessage(messageWithoutMessageHeader);
        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate, only()).convertAndSend("unhandled", message);
    }

    @Test
    void shouldSendMessageWithoutSoapHeaderToUnhandledQueue() throws JMSException, IOException {
        String messageWithoutSoapHeader = dataLoader.getData("ehrRequestWithoutSoapHeader.xml");
        JmsConsumer jmsConsumer = new JmsConsumer(mockJmsTemplate, "outbound", "unhandled");
        ActiveMQBytesMessage message = getActiveMQBytesMessage(messageWithoutSoapHeader);
        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate, only()).convertAndSend("unhandled", message);
    }
}
