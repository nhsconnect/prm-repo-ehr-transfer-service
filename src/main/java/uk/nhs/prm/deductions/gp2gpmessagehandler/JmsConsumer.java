package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@Component
public class JmsConsumer {

    final JmsTemplate jmsTemplate;

    @Value("${activemq.outboundQueue}")
    private String outboundQueue;

    public JmsConsumer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    //might need to use BytesMessage as that's how is probably gonna be read from the q
    @JmsListener(destination = "${activemq.inboundQueue}")
    public void onMessage(Message message) {
        try {
            BytesMessage bytesMessage = (BytesMessage) message;
            System.out.println("Received Message from Inbound queue");
            jmsTemplate.convertAndSend(outboundQueue, bytesMessage);
        } catch(Exception e) {
            System.out.println("Received Exception : "+ e);
        }
    }
}
