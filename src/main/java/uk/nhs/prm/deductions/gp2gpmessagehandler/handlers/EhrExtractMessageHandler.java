package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpmessagemodels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.EhrRepoService;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.GPToRepoClient;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.HttpException;

import static net.logstash.logback.argument.StructuredArguments.v;

@Service
@Slf4j
public class EhrExtractMessageHandler implements MessageHandler {

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
                log.info("Successfully stored large EHR extract message");
                gpToRepoClient.sendContinueMessage(parsedMessage.getMessageId(), parsedMessage.getConversationId());
                log.info("Successfully sent continue message");
            } else {
                ehrRepoService.storeMessage(parsedMessage);
                log.info("Successfully stored small EHR extract message");
                gpToRepoClient.notifySmallEhrExtractArrived(parsedMessage.getMessageId(), parsedMessage.getConversationId());
                log.info("Small ehr extract arrived notification sent");
            }
        } catch (HttpException | RuntimeException e) {
            log.warn("Sending EHR extract message to the unhandled queue", e, v("queue", unhandledQueue));
            jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
        }
    }
}
