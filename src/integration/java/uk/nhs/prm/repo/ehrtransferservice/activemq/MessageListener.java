package uk.nhs.prm.repo.ehrtransferservice.activemq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Map;

@Component
public class MessageListener implements javax.jms.MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

//    @JmsListener(destination = "queue-listener")
    @JmsListener(destination = "queue-1")
    public void sampleJmsListenerMethod(TextMessage message) throws JMSException {
        logger.info("JMS listener received text message: {}", message.getText());
    }

    @Override
    public void onMessage(Message message) {

    }

    //    @JmsListener(destination = "queue-1")
//    public void sampleJmsListenerMethod(Message message, @Headers Map<String, Object> headers) throws JMSException {
//        logger.info("JMS listener received text message: {}", message);
//    }
}