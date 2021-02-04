package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.JMSException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class JmsConsumerTest {

    @Mock
    JmsTemplate mockJmsTemplate;

    @Test
    void shouldSendMessageToOutboundQueue() throws JMSException {
        JmsConsumer jmsConsumer = new JmsConsumer(mockJmsTemplate, "outbound", "unhandled");
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        message.writeUTF("hello");
        message.reset();
        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate).convertAndSend("unhandled", message);
    }
}
