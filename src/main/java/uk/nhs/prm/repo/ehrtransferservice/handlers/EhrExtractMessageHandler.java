package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.JmsProducer;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.gp_to_repo.GPToRepoClient;

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
            //TODO: this class will be deleted. Putting a guard to break dependency for small message code execution
            if (!parsedMessage.isLargeMessage()) {
                return;
            }

            ehrRepoService.storeMessage(parsedMessage);
            log.info("Successfully stored large EHR extract message");
            gpToRepoClient.sendContinueMessage(parsedMessage.getMessageId(), parsedMessage.getConversationId());
            log.info("Successfully sent continue message");
        } catch (Exception e) {
            log.warn("Sending EHR extract message to the unhandled queue", e, v("queue", unhandledQueue));
            jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
        }
    }
}
