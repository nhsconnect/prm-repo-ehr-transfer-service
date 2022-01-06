package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.GPToRepoClient;

import static net.logstash.logback.argument.StructuredArguments.v;

@Service
public class PdsUpdateCompletedMessageHandler implements MessageHandler {
    private static Logger logger = LogManager.getLogger(PdsUpdateCompletedMessageHandler.class);

    private final JmsProducer jmsProducer;
    private String unhandledQueue;
    private GPToRepoClient gpToRepoClient;

    public PdsUpdateCompletedMessageHandler(JmsProducer jmsProducer, GPToRepoClient gpToRepoClient, @Value("${activemq.unhandledQueue}") String unhandledQueue){
        this.jmsProducer = jmsProducer;
        this.unhandledQueue = unhandledQueue;
        this.gpToRepoClient = gpToRepoClient;
    }

    @Override
    public String getInteractionId() {
        return "PRPA_IN000202UK01";
    }

    @Override
    public void handleMessage(ParsedMessage parsedMessage) {
        try {
            gpToRepoClient.sendPdsUpdatedMessage(parsedMessage.getConversationId());
            logger.info("Successfully notified gp-to-repo that PDS Update completed", v("conversationId", parsedMessage.getConversationId()));
        } catch (Exception e) {
            logger.info("Sending message to the queue", v("queue", unhandledQueue));
            logger.error("Failed to notify gp-to-repo about pds update completed", v("conversationId", parsedMessage.getConversationId()), e);
            jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
            throw new RuntimeException("Failed to notify gp-to-repo about pds update completed", e);
        }
    }
}
