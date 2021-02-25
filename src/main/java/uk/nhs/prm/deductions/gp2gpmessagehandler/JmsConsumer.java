package uk.nhs.prm.deductions.gp2gpmessagehandler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static net.logstash.logback.argument.StructuredArguments.v;
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

@Component
public class JmsConsumer {
    final JmsTemplate jmsTemplate;
    private String outboundQueue;
    private String inboundQueue;
    private String unhandledQueue;
    private static Logger logger = LogManager.getLogger(JmsConsumer.class);

    public JmsConsumer(JmsTemplate jmsTemplate, @Value("${activemq.outboundQueue}") String outboundQueue, @Value("${activemq.unhandledQueue}") String unhandledQueue, @Value("${activemq.inboundQueue}") String inboundQueue) {
        this.jmsTemplate = jmsTemplate;
        this.outboundQueue = outboundQueue;
        this.unhandledQueue = unhandledQueue;
        this.inboundQueue = inboundQueue;
    }

    @JmsListener(destination = "${activemq.inboundQueue}")
    public void onMessage(Message message) throws JMSException {
        MessageSanitizer messageSanitizer = new MessageSanitizer();

        BytesMessage bytesMessage = (BytesMessage) message;

        logger.info("Received Message from Inbound queue", v("queue", inboundQueue), v("correlationId", bytesMessage.getJMSCorrelationID()));

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
                logger.warn("Sending message without soap envelope header to unhandled queue", v("queue", unhandledQueue));
                jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
                return;
            }

            String interactionId = soapEnvelope.header.messageHeader.action;
            boolean knownInteractionId = Arrays.stream(InteractionIds.values())
                    .anyMatch(value -> value.getInteractionId().equals(interactionId));

            if (interactionId == null || !knownInteractionId) {
                logger.warn("Sending message with an unknown or missing interactionId to unhandled queue", v("queue", unhandledQueue));
                jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
                return;
            }

            logger.info("Sending message to outbound queue", v("queue", outboundQueue));
            jmsTemplate.convertAndSend(outboundQueue, bytesMessage);
        } catch (MessagingException | JsonParseException e) {
            logger.error(e.getMessage());
            jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
        } catch (JsonMappingException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (JMSException | IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
