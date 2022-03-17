package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.GPToRepoClient;

import static net.logstash.logback.argument.StructuredArguments.v;

@Service
@Slf4j
public class PdsUpdateCompletedMessageHandler implements MessageHandler {

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
            log.info("Successfully notified gp-to-repo that PDS Update completed", v("conversationId", parsedMessage.getConversationId()));
        } catch (Exception e) {
            log.info("Sending message to the queue", v("queue", unhandledQueue));
            log.error("Failed to notify gp-to-repo about pds update completed", v("conversationId", parsedMessage.getConversationId()), e);
            jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
            throw new RuntimeException("Failed to notify gp-to-repo about pds update completed", e);
        }
    }
}
