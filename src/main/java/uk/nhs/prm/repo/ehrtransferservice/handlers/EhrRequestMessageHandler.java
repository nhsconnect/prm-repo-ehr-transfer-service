package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.JmsProducer;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.HttpException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.repo_to_gp.RepoToGPClient;

import java.io.IOException;
import java.net.URISyntaxException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EhrRequestMessageHandler implements MessageHandler<ParsedMessage> {

    private final RepoToGPClient repoToGPClient;
    private final JmsProducer jmsProducer;
    @Value("${activemq.unhandledQueue}")
    private String unhandledQueue;

    @Override
    public void handleMessage(ParsedMessage parsedMessage) {
        try {
            repoToGPClient.sendEhrRequest(parsedMessage);
            log.info("Successfully sent EHR request to repo-to-gp");
        } catch (HttpException | URISyntaxException | IOException | InterruptedException e) {
            log.error("Failed to send the registration request", e);
            log.info("Sending message to the unhandled queue");
            jmsProducer.sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
        }
    }
}
