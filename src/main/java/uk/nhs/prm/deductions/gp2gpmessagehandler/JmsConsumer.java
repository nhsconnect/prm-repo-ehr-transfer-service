package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static net.logstash.logback.argument.StructuredArguments.v;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.handlers.MessageHandler;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.ParserService;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/*
 Responsible for:
  - interactions with the queue
  - deciding where to put each message
 */
@Component
public class JmsConsumer {
    final JmsProducer jmsProducer;
    private String inboundQueue;
    private String unhandledQueue;
    private static Logger logger = LogManager.getLogger(JmsConsumer.class);
    final MessageSanitizer messageSanitizer;
    final ParserService parserService;
    // can't initialize the dictionary in the constructor because it makes mocking harder
    private final List<MessageHandler> handlersList;
    private Dictionary<String, MessageHandler> handlers;

    public JmsConsumer(JmsProducer jmsProducer, @Value("${activemq.unhandledQueue}") String unhandledQueue, @Value("${activemq.inboundQueue}") String inboundQueue, MessageSanitizer messageSanitizer, ParserService parserService, List<MessageHandler> handlers) {
        this.jmsProducer = jmsProducer;
        this.unhandledQueue = unhandledQueue;
        this.inboundQueue = inboundQueue;
        this.messageSanitizer = messageSanitizer;
        this.parserService = parserService;
        this.handlersList  = handlers;
    }

    @JmsListener(destination = "${activemq.inboundQueue}")
    public void onMessage(Message message) throws JMSException {
        BytesMessage bytesMessage = (BytesMessage) message;
        byte[] contentAsBytes = new byte[(int) bytesMessage.getBodyLength()];
        bytesMessage.readBytes(contentAsBytes);
        String rawMessage = new String(contentAsBytes, StandardCharsets.UTF_8);

        logger.info("Received Message from Inbound queue", v("queue", inboundQueue), v("correlationId", bytesMessage.getJMSCorrelationID()));

        try {
            String sanitizedMessage = messageSanitizer.sanitize(contentAsBytes);
            ParsedMessage parsedMessage = parserService.parse(sanitizedMessage, rawMessage);
            logger.info("Successfully parsed message");
            String interactionId = parsedMessage.getAction();

            if (interactionId == null) {
                logger.warn("Sending message without soap envelope header to unhandled queue", v("queue", unhandledQueue));
                jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
                return;
            }

            MessageHandler matchingHandler = this.getHandlers().get(interactionId);

            if (matchingHandler == null) {
                logger.warn("Sending message with an unknown or missing interactionId to unhandled queue", v("queue", unhandledQueue), v("interactionId", interactionId));
                jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
                return;
            }

            matchingHandler.handleMessage(parsedMessage);
        } catch (RuntimeException | IOException e) {
            logger.error("Failed to process message from the queue", e);
            jmsProducer.sendMessageToQueue(unhandledQueue, rawMessage);
        }
    }

    private Dictionary<String, MessageHandler> getHandlers() {
        if(handlers == null) {
            this.handlers = new Hashtable<>();
            for(MessageHandler h : this.handlersList) {
                this.handlers.put(h.getInteractionId(), h);
            }
        }
        return handlers;
    }
}
