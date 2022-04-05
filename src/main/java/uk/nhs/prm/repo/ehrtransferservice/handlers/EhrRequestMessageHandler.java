package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.JmsProducer;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.HttpException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.repo_to_gp.RepoToGPClient;

import java.io.IOException;
import java.net.URISyntaxException;

import static net.logstash.logback.argument.StructuredArguments.v;

@Service
@Slf4j
public class EhrRequestMessageHandler implements MessageHandler {

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
            log.info("Successfully sent EHR request to repo-to-gp", v("conversationId", parsedMessage.getConversationId()));
        } catch (HttpException | URISyntaxException | IOException | InterruptedException e) {
            log.error("Failed to send the registration request", e);
            log.info("Sending message to the unhandled queue", v("queue", unhandledQueue));
            jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
        }
    }
}
