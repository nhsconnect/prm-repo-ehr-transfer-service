package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.GPToRepoClient;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.HttpException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

@Service
public class PdsUpdateCompletedMessageHandler extends JsQueueMessageHandler {
    private GPToRepoClient gpToRepoClient;

    public PdsUpdateCompletedMessageHandler(JmsProducer jmsProducer, GPToRepoClient gpToRepoClient, @Value("${activemq.outboundQueue}") String outboundQueue){
        super(jmsProducer, outboundQueue);
        this.gpToRepoClient = gpToRepoClient;
    }

    @Override
    public String getInteractionId() {
        return "PRPA_IN000202UK01";
    }

    @Override
    public void handleMessage(ParsedMessage parsedMessage) {
        try {
            gpToRepoClient.sendPdsUpdated(parsedMessage.getConversationId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to notify gp-to-repo about pds update completed", e);
        }
        super.handleMessage(parsedMessage);
    }
}
