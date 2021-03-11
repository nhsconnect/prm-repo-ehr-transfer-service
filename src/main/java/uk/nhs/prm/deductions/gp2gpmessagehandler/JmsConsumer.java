package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static net.logstash.logback.argument.StructuredArguments.v;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.ParserService;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Arrays;

/*
 Responsible for:
  - interactions with the queue
  - deciding where to put each message
 */
@Component
public class JmsConsumer {
    final JmsTemplate jmsTemplate;
    private String outboundQueue;
    private String inboundQueue;
    private String unhandledQueue;
    private static Logger logger = LogManager.getLogger(JmsConsumer.class);
    final MessageSanitizer messageSanitizer;
    final ParserService parserService;

    public JmsConsumer(JmsTemplate jmsTemplate, @Value("${activemq.outboundQueue}") String outboundQueue, @Value("${activemq.unhandledQueue}") String unhandledQueue, @Value("${activemq.inboundQueue}") String inboundQueue, MessageSanitizer messageSanitizer, ParserService parserService) {
        this.jmsTemplate = jmsTemplate;
        this.outboundQueue = outboundQueue;
        this.unhandledQueue = unhandledQueue;
        this.inboundQueue = inboundQueue;
        this.messageSanitizer = messageSanitizer;
        this.parserService = parserService;
    }

    @JmsListener(destination = "${activemq.inboundQueue}")
    public void onMessage(Message message) throws JMSException {
        BytesMessage bytesMessage = (BytesMessage) message;

        logger.info("Received Message from Inbound queue", v("queue", inboundQueue), v("correlationId", bytesMessage.getJMSCorrelationID()));

        try {
            byte[] contentAsBytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(contentAsBytes);
            String sanitizedMessage = messageSanitizer.sanitize(contentAsBytes);
            ParsedMessage parsedMessage = parserService.parse(sanitizedMessage);

            String interactionId = parsedMessage.getAction();

            if (interactionId == null) {
                logger.warn("Sending message without soap envelope header to unhandled queue", v("queue", unhandledQueue));
                jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
                return;
            }

            boolean knownInteractionId = Arrays.stream(InteractionIds.values())
                    .anyMatch(value -> value.getInteractionId().equals(interactionId));

            if (!knownInteractionId) {
                logger.warn("Sending message with an unknown or missing interactionId to unhandled queue", v("queue", unhandledQueue));
                jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
                return;
            }

            logger.info("Sending message to outbound queue", v("queue", outboundQueue));
            jmsTemplate.convertAndSend(outboundQueue, bytesMessage);
        } catch (Exception e) {
            logger.error("Failed to process message from the queue", e);
            jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
        }
    }
}
