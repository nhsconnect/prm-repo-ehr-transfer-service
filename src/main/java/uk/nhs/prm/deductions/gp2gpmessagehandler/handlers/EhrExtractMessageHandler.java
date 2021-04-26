package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.EhrRepoService;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.GPToRepoClient;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.HttpException;

import static net.logstash.logback.argument.StructuredArguments.v;

@Service
public class EhrExtractMessageHandler implements MessageHandler {
    private static Logger logger = LogManager.getLogger(EhrExtractMessageHandler.class);

    private final JmsProducer jmsProducer;
    private String unhandledQueue;
    private GPToRepoClient gpToRepoClient;
    private EhrRepoService ehrRepoService;

    public EhrExtractMessageHandler(JmsProducer jmsProducer, @Value("${activemq.unhandledQueue}") String unhandledQueue, GPToRepoClient gpToRepoClient, EhrRepoService ehrRepoService) {
        this.jmsProducer = jmsProducer;
        this.gpToRepoClient = gpToRepoClient;
        this.unhandledQueue = unhandledQueue;
        this.ehrRepoService = ehrRepoService;
    }

    @Override
    public String getInteractionId() {
        return "RCMR_IN030000UK06";
    }

    @Override
    public void handleMessage(ParsedMessage parsedMessage) {
        try {
            if (parsedMessage.isLargeMessage()) {
                ehrRepoService.storeMessage(parsedMessage);
                logger.info("Successfully stored large EHR extract message");
                gpToRepoClient.sendContinueMessage(parsedMessage.getMessageId(), parsedMessage.getConversationId());
                logger.info("Successfully sent continue message");
            } else {
                ehrRepoService.storeMessage(parsedMessage);
                logger.info("Successfully stored small EHR extract message");
                gpToRepoClient.notifySmallEhrExtractArrived(parsedMessage.getMessageId(), parsedMessage.getConversationId());
                logger.info("Small ehr extract arrived notification sent");
            }
        } catch (HttpException | RuntimeException e) {
            logger.warn("Sending EHR extract message to the queue", v("queue", unhandledQueue));
            jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
        }
    }
}
