package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.EhrExtractMessageWrapper;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.GPToRepoClient;

import javax.jms.BytesMessage;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.v;

/*
 Can handle gp2gp message: RCMR_IN030000UK06
 */
@Service
public class EhrExtractMessageHandler implements MessageHandler {
    private static Logger logger = LogManager.getLogger(EhrExtractMessageHandler.class);

    private final JmsTemplate jmsTemplate;
    private String outboundQueue;
    private String unhandledQueue;
    private GPToRepoClient gpToRepoClient;

    public EhrExtractMessageHandler(JmsTemplate jmsTemplate, @Value("${activemq.outboundQueue}") String outboundQueue,  @Value("${activemq.unhandledQueue}") String unhandledQueue, GPToRepoClient gpToRepoClient) {
        this.jmsTemplate = jmsTemplate;
        this.outboundQueue = outboundQueue;
        this.gpToRepoClient = gpToRepoClient;
        this.unhandledQueue = unhandledQueue;
    }

    @Override
    public String getInteractionId() {
        return "RCMR_IN030000UK06";
    }

    @Override
    public void handleMessage(ParsedMessage parsedMessage, BytesMessage bytesMessage) {
        if (parsedMessage.isLargeMessage()) {
            try {
                UUID conversationId = parsedMessage.getConversationId();
                UUID ehrExtractMessageId = parsedMessage.getMessageId();
                gpToRepoClient.sendContinueMessage(ehrExtractMessageId, conversationId);
            } catch (Exception e) {
                logger.error("Failed to send continue message to GP To Repo", e);
                jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
            }
        } else {
            logger.info("Sending message to outbound queue", v("queue", outboundQueue));
            jmsTemplate.convertAndSend(outboundQueue, bytesMessage);
        }
    }
}
