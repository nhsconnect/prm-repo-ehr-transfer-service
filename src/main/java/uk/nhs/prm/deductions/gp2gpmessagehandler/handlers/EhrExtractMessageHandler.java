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

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static net.logstash.logback.argument.StructuredArguments.v;

@Service
public class EhrExtractMessageHandler implements MessageHandler {
    private static Logger logger = LogManager.getLogger(EhrExtractMessageHandler.class);

    private final JmsProducer jmsProducer;
    private String outboundQueue;
    private String unhandledQueue;
    private GPToRepoClient gpToRepoClient;
    private EhrRepoService ehrRepoService;

    public EhrExtractMessageHandler(JmsProducer jmsProducer, @Value("${activemq.outboundQueue}") String outboundQueue, @Value("${activemq.unhandledQueue}") String unhandledQueue, GPToRepoClient gpToRepoClient, EhrRepoService ehrRepoService) {
        this.jmsProducer = jmsProducer;
        this.outboundQueue = outboundQueue;
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
                logger.info("Successfully stored message");
                gpToRepoClient.sendContinueMessage(parsedMessage.getMessageId(), parsedMessage.getConversationId());
                logger.info("Successfully sent continue message");
            } else {
                logger.info("Sending message to outbound queue", v("queue", outboundQueue));
                jmsProducer.sendMessageToQueue(outboundQueue, parsedMessage.getRawMessage());
            }
        } catch (HttpException | MalformedURLException | URISyntaxException | RuntimeException e) {
            logger.error("Failed to store message and send continue request", e);
            jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
        }
    }
}
