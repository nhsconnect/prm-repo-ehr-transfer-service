package uk.nhs.prm.deductions.gp2gpmessagehandler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class JmsConsumer {

    private static final Logger logger = LogManager.getLogger();

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

        @Override
        public String toString() {
            return "SOAPEnvelope{" +
                    "header=" + header +
                    '}';
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SOAPHeader {
        @JacksonXmlProperty(localName = "MessageHeader", namespace = "eb")
        MessageHeader messageHeader;

        @Override
        public String toString() {
            return "SOAPHeader{" +
                    "messageHeader=" + messageHeader +
                    '}';
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class MessageHeader {
        @JacksonXmlProperty(localName = "Action", namespace = "eb")
        String action;

        @Override
        public String toString() {
            return "MessageHeader{" +
                    "action='" + action + '\'' +
                    '}';
        }
    }

    @JmsListener(destination = "${activemq.inboundQueue}")
    public void onMessage(Message message) {
        MessageSanitizer messageSanitizer = new MessageSanitizer();

        BytesMessage bytesMessage = (BytesMessage) message;

        System.out.println("Received Message from Inbound queue");
        logger.info("Hello from Log4j 2");

        try {
            byte[] contentAsBytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(contentAsBytes);
            String fullContent = new String(contentAsBytes, StandardCharsets.UTF_8);
            ByteArrayDataSource dataSource = new ByteArrayDataSource(messageSanitizer.sanitize(fullContent), "multipart/related;charset=\"UTF-8\"");
            MimeMultipart mimeMultipart = new MimeMultipart(dataSource);
            BodyPart soapHeader = mimeMultipart.getBodyPart(0);
            XmlMapper xmlMapper = new XmlMapper();
            InputStream inputStream = soapHeader.getInputStream();
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            SOAPEnvelope soapEnvelope = xmlMapper.readValue(content, SOAPEnvelope.class);

            if (soapEnvelope.header == null || soapEnvelope.header.messageHeader == null || soapEnvelope.header.messageHeader.action == null) {
                System.out.println("Sending message without soap envelope header to unhandled queue");
                jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
                return;
            }

            System.out.println("Sending message to outbound queue");
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
