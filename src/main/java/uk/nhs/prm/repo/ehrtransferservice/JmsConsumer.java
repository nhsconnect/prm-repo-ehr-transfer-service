package uk.nhs.prm.repo.ehrtransferservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.codec.ReadableBuffer;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.ParsingDlqPublisher;
import uk.nhs.prm.repo.ehrtransferservice.services.Broker;
import uk.nhs.prm.repo.ehrtransferservice.parsers.MessageSanitizer;
import uk.nhs.prm.repo.ehrtransferservice.parsers.Parser;

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
        String rawMessage = "<NOT-PARSED-YET>";
        try {
            rawMessage = getRawMessage(message);
            log.info("Received Message from Inbound queue");
            var parsedMessage = parser.parse(rawMessage);
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
            attemptAmqpDecode(contentAsBytes);
            // This logic (usage of sanitizer) was in the upper function call.
            // Moved here to allow AMQP decoding to not depend on it.
            var contentForcedAsUtfString = new String(contentAsBytes, StandardCharsets.UTF_8);
            return messageSanitizer.sanitize(contentForcedAsUtfString.getBytes(StandardCharsets.UTF_8));
        }

        log.info("Received TextMessage from MQ");
        var textMessage = (TextMessage) message;
        log.info(textMessage.getText());
        return messageSanitizer.sanitize(textMessage.getText().getBytes(StandardCharsets.UTF_8));
    }

    private void attemptAmqpDecode(byte[] contentAsBytes) {
        var byteBuffer = ReadableBuffer.ByteBufferReader.wrap(contentAsBytes);
        var amqpMessage = org.apache.qpid.proton.message.Message.Factory.create();
        try {
            amqpMessage.decode(byteBuffer);
            log.info("decoded as AMQP message, type is: " + amqpMessage.getBody().getType());
            var amqpValueAsString = (String)((AmqpValue)amqpMessage.getBody()).getValue();
            log.info("we've been able to parse body:", amqpValueAsString.substring(0, 15));
        }
        catch (Exception e) {
            log.info("failed to decode as AMQP message", e);
        }
    }

    private void debugMessageFormatInfo(Message message, Map<String, Object> headers) throws JMSException {
        log.info("Class of inbound message: " + message.getClass().getName());
        log.info("JMS correlation id: " + message.getJMSCorrelationID());

        log.info("property names: " + Collections.list(message.getPropertyNames()).stream().collect(Collectors.joining(", ")));

        log.info("correlation-id property: " + message.getStringProperty("correlation-id"));
        log.info("JMS_AMQP_NATIVE property: " + message.getStringProperty("JMS_AMQP_NATIVE"));
        log.info("message-format property: " + message.getStringProperty("message-format"));
        log.info("JMS_AMQP_ContentType property: " + message.getStringProperty("JMS_AMQP_ContentType"));
        log.info("JMS_AMQP_HEADER property: " + message.getStringProperty("JMS_AMQP_HEADER"));

        log.info("headers: " + headers.entrySet().stream().map(kv -> kv.getKey() + ":" + kv.getValue()).collect(Collectors.joining(", ")));

        log.info("correlation-id header: " + headers.get("correlation-id"));
        log.info("JMS_AMQP_NATIVE header: " + headers.get("JMS_AMQP_NATIVE"));
        log.info("message-format header: " + headers.get("message-format"));
    }
}