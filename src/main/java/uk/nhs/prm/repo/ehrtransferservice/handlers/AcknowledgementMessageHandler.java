package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.JmsProducer;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.v;

@Service
@Slf4j
public class AcknowledgementMessageHandler implements MessageHandler {

    private final JmsProducer jmsProducer;
    private String unhandledQueue;

    public AcknowledgementMessageHandler(JmsProducer jmsProducer, @Value("${activemq.unhandledQueue}") String unhandledQueue) {
        this.jmsProducer = jmsProducer;
        this.unhandledQueue = unhandledQueue;
    }

    @Override
    public String getInteractionId() {
        return "MCCI_IN010000UK13";
    }

    @Override
    public void handleMessage(ParsedMessage parsedMessage) {
        List<String> errorReasons = parsedMessage.getReasons();
        if (errorReasons != null && errorReasons.size() > 0) {
            String errors = Strings.join(errorReasons, ';');
            log.error("Found error reasons in acknowledgement message: " + errors, v("conversationId", parsedMessage.getConversationId()));
        }
        log.info("Sending message to the queue", v("queue", unhandledQueue), v("conversationId", parsedMessage.getConversationId()));
        jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
    }
}
