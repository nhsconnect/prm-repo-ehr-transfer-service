package uk.nhs.prm.repo.ehrtransferservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.ParsingDlqPublisher;
import uk.nhs.prm.repo.ehrtransferservice.parsers.Parser;
import uk.nhs.prm.repo.ehrtransferservice.services.Broker;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JmsConsumer {
    final JmsProducer jmsProducer;
    final Parser parser;
    private final Broker broker;
    private final Tracer tracer;
    private final ParsingDlqPublisher parsingDlqPublisher;
    private final String unprocessableMessageBody = "NO_ACTION:UNPROCESSABLE_MESSAGE_BODY";

    @JmsListener(destination = "${activemq.inboundQueue}")
    public void onMessage(Message message,
                          @Headers Map<String, Object> headers) throws JMSException {
        tracer.setMDCContextFromMhsInbound(message.getJMSCorrelationID());
        debugMessageFormatInfo(message, headers);
        String messageBody = null;
        try {
            // TODO: single call to parser
            messageBody = parser.parseMessageBody(message);
            log.info("Received Message from Inbound queue");
            var parsedMessage = parser.parse(messageBody);

            tracer.handleConversationId(parsedMessage.getConversationId().toString());
            log.info("Successfully parsed message");

            if (parsedMessage.getInteractionId() == null || parsedMessage.getInteractionId().isBlank()) {
                log.warn("Sending message without Interaction ID to dlq");
                parsingDlqPublisher.sendMessage(messageBody);
                return;
            }

            broker.sendMessageToCorrespondingTopicPublisher(parsedMessage);
        } catch (Exception e) {
            var toBeSentToDlq = messageBody != null ? messageBody : unprocessableMessageBody;
            // TODO: don't log messages (here and anywhere else)
            // + wrap specific case of problem parsing message around the specific code that does it?
            log.error("Failed to process message - sending to dlq", e);
            log.error("Message content: " + toBeSentToDlq);
            parsingDlqPublisher.sendMessage(toBeSentToDlq);
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