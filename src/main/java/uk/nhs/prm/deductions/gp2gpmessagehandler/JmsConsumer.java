package uk.nhs.prm.deductions.gp2gpmessagehandler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.SOAPEnvelope;

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
import java.util.Arrays;
import java.util.List;

@Component
public class JmsConsumer {

    final JmsTemplate jmsTemplate;
    private String outboundQueue;
    private String unhandledQueue;
    private static Logger logger = LogManager.getLogger("JSON_LAYOUT_APPENDER");

    final String EHR_REQUEST = "RCMR_IN010000UK05";
    final String EHR_REQUEST_COMPLETED = "RCMR_IN030000UK06";
    final String PDS_GENERAL_UPDATE_REQUEST_ACCEPTED = "PRPA_IN000202UK01";

    List<String> validInteractionIds = Arrays.asList(EHR_REQUEST, EHR_REQUEST_COMPLETED, PDS_GENERAL_UPDATE_REQUEST_ACCEPTED);

    public JmsConsumer(JmsTemplate jmsTemplate, @Value("${activemq.outboundQueue}") String outboundQueue, @Value("${activemq.unhandledQueue}") String unhandledQueue) {
        this.jmsTemplate = jmsTemplate;
        this.outboundQueue = outboundQueue;
        this.unhandledQueue = unhandledQueue;
    }

    @JmsListener(destination = "${activemq.inboundQueue}")
    public void onMessage(Message message) {
        MessageSanitizer messageSanitizer = new MessageSanitizer();

        BytesMessage bytesMessage = (BytesMessage) message;

        logger.info("Received Message from Inbound queue");

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

            if (soapEnvelope.header == null || soapEnvelope.header.messageHeader == null) {
                logger.info("Sending message without soap envelope header to unhandled queue");
                jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
                return;
            }

            if (soapEnvelope.header.messageHeader.action == null || !validInteractionIds.contains(soapEnvelope.header.messageHeader.action)) {
                logger.info("Sending message with an invalid or missing interactionId to unhandled queue");
                jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
                return;
            }

            logger.info("Sending message to outbound queue");
            jmsTemplate.convertAndSend(outboundQueue, bytesMessage);
        } catch (MessagingException | JsonParseException e) {
            logger.error(e.getMessage());
            jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
        } catch (JMSException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (JsonMappingException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
