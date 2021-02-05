package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

@Component
public class JmsConsumer {

    final JmsTemplate jmsTemplate;
    private String outboundQueue;
    private String unhandledQueue;

    public JmsConsumer(JmsTemplate jmsTemplate, @Value("${activemq.outboundQueue}") String outboundQueue, @Value("${activemq.unhandledQueue}") String unhandledQueue) {
        this.jmsTemplate = jmsTemplate;
        this.outboundQueue = outboundQueue;
        this.unhandledQueue = unhandledQueue;
    }

    @JmsListener(destination = "${activemq.inboundQueue}")
    public void onMessage(Message message) {

        BytesMessage bytesMessage = (BytesMessage) message;
        System.out.println("Received Message from Inbound queue");

        try {
            byte[] contentAsBytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(contentAsBytes);
            ByteArrayDataSource dataSource = new ByteArrayDataSource(contentAsBytes, "multipart/related;charset=\"UTF-8\"");
            MimeMultipart mimeMultipart = new MimeMultipart(dataSource);
            mimeMultipart.getBodyPart(0);

            jmsTemplate.convertAndSend(outboundQueue, bytesMessage);
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
            jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
        } catch (JMSException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
