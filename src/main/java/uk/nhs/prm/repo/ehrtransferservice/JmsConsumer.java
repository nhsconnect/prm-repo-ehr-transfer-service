package uk.nhs.prm.repo.ehrtransferservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.handlers.MessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Broker;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Parser;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.v;

/*
 Responsible for:
  - interactions with the queue
  - deciding where to put each message
 */
@Component
@Slf4j
public class JmsConsumer {
    final JmsProducer jmsProducer;
    final MessageSanitizer messageSanitizer;
    final Parser parser;
    // can't initialize the dictionary in the constructor because it makes mocking harder
    private final List<MessageHandler> handlersList;
    private String inboundQueue;
    private String unhandledQueue;
    private Broker broker;
    private Dictionary<String, MessageHandler> handlers;

    public JmsConsumer(JmsProducer jmsProducer, @Value("${activemq.unhandledQueue}") String unhandledQueue, @Value("${activemq.inboundQueue}") String inboundQueue, MessageSanitizer messageSanitizer, Parser parser, Broker broker, List<MessageHandler> handlers) {
        this.jmsProducer = jmsProducer;
        this.unhandledQueue = unhandledQueue;
        this.inboundQueue = inboundQueue;
        this.messageSanitizer = messageSanitizer;
        this.parser = parser;
        this.broker = broker;
        this.handlersList = handlers;
    }

    @JmsListener(destination = "${activemq.inboundQueue}")
    public void onMessage(Message message) throws JMSException {
        String rawMessage = getRawMessage(message);
        log.info("Received Message from Inbound queue with correlation ID: " + message.getJMSCorrelationID());

        try {
            String sanitizedMessage = messageSanitizer.sanitize(rawMessage.getBytes(StandardCharsets.UTF_8));
            ParsedMessage parsedMessage = parser.parse(sanitizedMessage);
            log.info("Successfully parsed message");
            String interactionId = parsedMessage.getInteractionId();
            log.info(message.getJMSCorrelationID());

            if (interactionId == null) {
                log.warn("Sending message without soap envelope header to unhandled queue");
                jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
                return;
            }

            broker.sendMessageToCorrespondingTopicPublisher(interactionId, parsedMessage.getRawMessage(), parsedMessage.getConversationId(), parsedMessage.isLargeMessage(), parsedMessage.isNegativeAcknowledgement());

            MessageHandler matchingHandler = this.getHandlers().get(interactionId);

            if (matchingHandler == null) {
                log.warn("Sending message with an unknown or missing interactionId \"" + interactionId + "\" to unhandled queue");
                jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
                return;
            }

            matchingHandler.handleMessage(parsedMessage);
        } catch (RuntimeException | IOException e) {
            log.error("Failed to process message from the queue", e);
            jmsProducer.sendMessageToQueue(unhandledQueue, rawMessage);
        }
    }

    private String getRawMessage(Message message) throws JMSException {
        if (message instanceof BytesMessage) {
            log.info("Received BytesMessage from MQ");
            BytesMessage bytesMessage = (BytesMessage) message;
            byte[] contentAsBytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(contentAsBytes);
            String rawMessage = new String(contentAsBytes, StandardCharsets.UTF_8);
            return rawMessage;
        }
        log.info("Received TextMessage from MQ");
        var textMessage = (TextMessage) message;
        return textMessage.getText();
    }

    private Dictionary<String, MessageHandler> getHandlers() {
        if (handlers == null) {
            this.handlers = new Hashtable<>();
            for (MessageHandler h : this.handlersList) {
                this.handlers.put(h.getInteractionId(), h);
            }
        }
        return handlers;
    }
}
