package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class JmsConsumerTest {

    @Mock
    JmsTemplate mockJmsTemplate;

    @Value("${activemq.outboundQueue}")
    private String outboundQueue;

    @Test
    void shouldSendMessageToOutboundQueue() {
        JmsConsumer jmsConsumer = new JmsConsumer(mockJmsTemplate);
        ActiveMQBytesMessage message = new ActiveMQBytesMessage();
        jmsConsumer.onMessage(message);
        verify(mockJmsTemplate).convertAndSend(outboundQueue, message);
    }
}
