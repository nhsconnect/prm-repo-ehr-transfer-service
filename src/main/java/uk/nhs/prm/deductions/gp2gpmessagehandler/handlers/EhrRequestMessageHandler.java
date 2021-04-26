package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.HttpException;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.RepoToGPClient;

import java.io.IOException;
import java.net.URISyntaxException;

import static net.logstash.logback.argument.StructuredArguments.v;

@Service
public class EhrRequestMessageHandler implements MessageHandler {
    private static Logger logger = LogManager.getLogger(EhrExtractMessageHandler.class);

    private RepoToGPClient repoToGPClient;
    private String unhandledQueue;
    private JmsProducer jmsProducer;

    public EhrRequestMessageHandler(JmsProducer jmsProducer, RepoToGPClient repoToGPClient, @Value("${activemq.unhandledQueue}") String unhandledQueue) {
        this.jmsProducer = jmsProducer;
        this.unhandledQueue = unhandledQueue;
        this.repoToGPClient = repoToGPClient;
    }

    @Override
    public String getInteractionId() {
        return "RCMR_IN010000UK05";
    }

    @Override
    public void handleMessage(ParsedMessage parsedMessage) {
        try {
            repoToGPClient.sendEhrRequest(parsedMessage);
        } catch (HttpException | URISyntaxException | IOException | InterruptedException e) {
            logger.error("Failed to send the registration request", e);
            logger.info("Sending message to the unhandled queue", v("queue", unhandledQueue));
            jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
        }
    }
}
