package uk.nhs.prm.repo.ehrtransferservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.ParsingDlqPublisher;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Broker;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.MessageSanitizer;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Parser;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JmsConsumer {
    final JmsProducer jmsProducer;
    final MessageSanitizer messageSanitizer;
    final Parser parser;
    private final Broker broker;
    private final Tracer tracer;
    private final ParsingDlqPublisher parsingDlqPublisher;

    @JmsListener(destination = "${activemq.inboundQueue}")
    public void onMessage(Message message,
                          @Headers Map<String, Object> headers) throws JMSException {
        tracer.setMDCContextFromMhsInbound(message.getJMSCorrelationID());

        debugMessageFormatInfo(message, headers);

        var rawMessage = getRawMessage(message);

        log.info("Received Message from Inbound queue");

        try {
            var sanitizedMessage = messageSanitizer.sanitize(rawMessage.getBytes(StandardCharsets.UTF_8));
            var parsedMessage = parser.parse(sanitizedMessage);
            tracer.handleConversationId(parsedMessage.getConversationId().toString());
            log.info("Successfully parsed message");

            if (parsedMessage.getInteractionId() == null || parsedMessage.getInteractionId().isBlank()) {
                log.warn("Sending message without Interaction ID to dlq");
                parsingDlqPublisher.sendMessage(rawMessage);
                return;
            }

            broker.sendMessageToCorrespondingTopicPublisher(parsedMessage);
        } catch (Exception e) {
            log.error("Failed to process message - sending to dlq", e);
            log.error("Message content: " + rawMessage);
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
        log.info(textMessage.getText());
        return textMessage.getText();
    }

    private void debugMessageFormatInfo(Message message, Map<String, Object> headers) throws JMSException {
        log.info("Class of inbound message: " + message.getClass().getName());
        log.info("JMS correlation id: " + message.getJMSCorrelationID());

        log.info("property names: " + Collections.list(message.getPropertyNames()).stream().collect(Collectors.joining(", ")));

        log.info("correlation-id property: " + message.getStringProperty("correlation-id"));
        log.info("properties.correlation-id property: " + message.getStringProperty("properties.correlation-id"));
        log.info("JMSCorrelationID property: " + message.getStringProperty("JMSCorrelationID"));
        log.info("JMS_AMQP_NATIVE property: " + message.getStringProperty("JMS_AMQP_NATIVE"));
        log.info("message-format property: " + message.getStringProperty("message-format"));

        log.info("header names: " + headers.keySet().stream().collect(Collectors.joining(", ")));

        log.info("correlation-id header: " + headers.get("correlation-id"));
        log.info("properties.correlation-id header: " + headers.get("properties.correlation-id"));
        log.info("JMSCorrelationID header: " + headers.get("JMSCorrelationID"));
        log.info("JMS_AMQP_NATIVE header: " + headers.get("JMS_AMQP_NATIVE"));
        log.info("message-format header: " + headers.get("message-format"));
    }
}