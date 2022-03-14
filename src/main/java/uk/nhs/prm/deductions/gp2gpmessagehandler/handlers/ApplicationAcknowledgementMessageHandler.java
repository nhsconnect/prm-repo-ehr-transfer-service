package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.GPToRepoClient;

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.v;

@Service
public class ApplicationAcknowledgementMessageHandler implements MessageHandler {
    private static Logger logger = LogManager.getLogger(ApplicationAcknowledgementMessageHandler.class);

    private final JmsProducer jmsProducer;
    private String unhandledQueue;

    public ApplicationAcknowledgementMessageHandler(JmsProducer jmsProducer, @Value("${activemq.unhandledQueue}") String unhandledQueue){
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
        if(errorReasons != null && errorReasons.size() > 0){
            String errors = Strings.join(errorReasons, ';');
            logger.error("Found error reasons in acknowledgement message: " + errors, v("conversationId", parsedMessage.getConversationId()));
        }
        logger.info("Sending message to the queue", v("queue", unhandledQueue), v("conversationId", parsedMessage.getConversationId()));
        jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
    }
}