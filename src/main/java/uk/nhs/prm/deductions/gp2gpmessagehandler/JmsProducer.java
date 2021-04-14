package uk.nhs.prm.deductions.gp2gpmessagehandler;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import java.nio.charset.StandardCharsets;

@Component
public class JmsProducer {
    final JmsTemplate jmsTemplate;
    private static Logger logger = LogManager.getLogger(JmsProducer.class);

    public JmsProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    private ActiveMQBytesMessage generateBytesMessage(String message) throws JMSException {
        final byte[] bytesArray = message.getBytes(StandardCharsets.UTF_8);
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        bytesMessage.writeBytes(bytesArray);
        bytesMessage.reset();

        return bytesMessage;
    }

    public void sendMessageToQueue(String queueName, String message) {
        try {
            ActiveMQBytesMessage bytesMessage = generateBytesMessage(message);
            jmsTemplate.convertAndSend(queueName, bytesMessage);
        } catch(JMSException e) {
            logger.error(String.format("Failed to send message to the %s queue", queueName), e);
        }
    }
}
