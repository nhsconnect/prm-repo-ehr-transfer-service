package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.BytesMessage;
import javax.jms.Message;

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
            String content = bytesMessage.readUTF();
            System.out.println("Received Message: "+ content);
            jmsTemplate.convertAndSend(outboundQueue, content);
        } catch(Exception e) {
            System.out.println("Received Exception : "+ e);
        }
    }
}
