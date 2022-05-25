package uk.nhs.prm.repo.ehrtransferservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.handlers.MessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.ParsingDlqPublisher;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Broker;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.MessageSanitizer;
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
    private final String unhandledQueue;
    private final Broker broker;
    private final Tracer tracer;
    private final ParsingDlqPublisher parsingDlqPublisher;
    private Dictionary<String, MessageHandler> handlers;

    public JmsConsumer(JmsProducer jmsProducer, @Value("${activemq.unhandledQueue}") String unhandledQueue, @Value("${activemq.inboundQueue}") String inboundQueue, MessageSanitizer messageSanitizer, Parser parser, Broker broker, Tracer tracer, ParsingDlqPublisher parsingDlqPublisher, List<MessageHandler> handlers) {
        this.jmsProducer = jmsProducer;
        this.unhandledQueue = unhandledQueue;
        this.messageSanitizer = messageSanitizer;
        this.parser = parser;
        this.broker = broker;
        this.tracer = tracer;
        this.parsingDlqPublisher = parsingDlqPublisher;
        this.handlersList = handlers;
    }

    @JmsListener(destination = "${activemq.inboundQueue}")
    public void onMessage(Message message) throws JMSException {
        var rawMessage = getRawMessage(message);
        log.info("Received Message from Inbound queue");

        try {
            var sanitizedMessage = messageSanitizer.sanitize(rawMessage.getBytes(StandardCharsets.UTF_8));
            var parsedMessage = parser.parse(sanitizedMessage);
            tracer.setMDCContextFromMhsInbound(message.getJMSCorrelationID(), parsedMessage.getConversationId().toString());
            log.info("Successfully parsed message");
            log.info("Trace ID: "+message.getStringProperty("traceId")+" // "+message.getJMSCorrelationID()+" // Conversation ID: "+message.getStringProperty("conversationId")+" // "+parsedMessage.getConversationId().toString());

            if (parsedMessage.getInteractionId() == null) {
                log.warn("Sending message without Interaction ID to unhandled queue");
                jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
                return;
            }

            broker.sendMessageToCorrespondingTopicPublisher(parsedMessage);
//            var matchingHandler = this.getHandlers().get(parsedMessage.getInteractionId());
//
//            if (matchingHandler == null) {
//                log.warn("Sending message with an unknown or missing interactionId \"" + parsedMessage.getInteractionId() + "\" to unhandled queue");
//                jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
//                return;
//            }
//
//            matchingHandler.handleMessage(parsedMessage);
        } catch (Exception e) {
            log.error("Failed to process message - sending to dlq", e);
            parsingDlqPublisher.sendMessage(rawMessage);
        }
    }

    private String getRawMessage(Message message) throws JMSException {
        if (message instanceof BytesMessage) {
            log.info("Received BytesMessage from MQ");
            var bytesMessage = (BytesMessage) message;
            byte[] contentAsBytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(contentAsBytes);
            return new String(contentAsBytes, StandardCharsets.UTF_8);
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