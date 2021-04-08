package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.EhrRepoService;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

import javax.jms.BytesMessage;

@Service
public class CopcMessageHandler implements MessageHandler {
    private static Logger logger = LogManager.getLogger(EhrExtractMessageHandler.class);

    private final JmsTemplate jmsTemplate;
    private EhrRepoService ehrRepoService;
    private String unhandledQueue;

    public CopcMessageHandler(JmsTemplate jmsTemplate, EhrRepoService ehrRepoService, @Value("${activemq.unhandledQueue}") String unhandledQueue) {
        this.jmsTemplate = jmsTemplate;
        this.unhandledQueue = unhandledQueue;
        this.ehrRepoService = ehrRepoService;
    }

    @Override
    public String getInteractionId() {
        return "COPC_IN000001UK01";
    }

    @Override
    public void handleMessage(ParsedMessage parsedMessage) {
        BytesMessage bytesMessage = null;
        try {
            bytesMessage = parsedMessage.getBytesMessage();
            ehrRepoService.storeMessage(parsedMessage);
            logger.info("Successfully stored copc message");
        } catch (Exception e) {
            logger.error("Failed to store copc message", e);
            jmsTemplate.convertAndSend(unhandledQueue, bytesMessage);
        }
    }
}
