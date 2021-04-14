package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.EhrRepoService;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.HttpException;

@Service
public class CopcMessageHandler implements MessageHandler {
    private static Logger logger = LogManager.getLogger(EhrExtractMessageHandler.class);

    private EhrRepoService ehrRepoService;
    private String unhandledQueue;
    private JmsProducer jmsProducer;

    public CopcMessageHandler(JmsProducer jmsProducer, EhrRepoService ehrRepoService, @Value("${activemq.unhandledQueue}") String unhandledQueue) {
        this.jmsProducer = jmsProducer;
        this.unhandledQueue = unhandledQueue;
        this.ehrRepoService = ehrRepoService;
    }

    @Override
    public String getInteractionId() {
        return "COPC_IN000001UK01";
    }

    @Override
    public void handleMessage(ParsedMessage parsedMessage) {
        try {
            ehrRepoService.storeMessage(parsedMessage);
            logger.info("Successfully stored copc message");
        } catch (HttpException e) {
            logger.error("Failed to store copc message", e);
            jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
        }
    }
}
