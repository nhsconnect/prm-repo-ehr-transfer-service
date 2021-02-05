package uk.nhs.prm.deductions.gp2gpmessagehandler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
import java.io.IOException;

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SOAPEnvelope {
        @JacksonXmlProperty(localName = "Header", namespace = "SOAP-ENV")
        SOAPHeader header;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SOAPHeader {
        @JacksonXmlProperty(localName = "MessageHeader", namespace = "eb")
        MessageHeader messageHeader;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class MessageHeader {
        @JacksonXmlProperty(localName = "Action", namespace = "eb")
        String action;
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
            BodyPart soapHeader = mimeMultipart.getBodyPart(0);
            XmlMapper xmlMapper = new XmlMapper();
            SOAPEnvelope soapEnvelope = xmlMapper.readValue(soapHeader.getInputStream(), SOAPEnvelope.class);

            if (soapEnvelope.header.messageHeader.action == null) {
                jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
                return;
            }

            jmsTemplate.convertAndSend(outboundQueue, bytesMessage);
        } catch (MessagingException | JsonParseException e) {
            System.out.println(e.getMessage());
            jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
        } catch (JMSException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
