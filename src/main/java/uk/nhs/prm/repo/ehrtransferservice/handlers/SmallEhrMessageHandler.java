package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
@Service
@Slf4j
public class SmallEhrMessageHandler implements MessageHandler {

    private EhrRepoService ehrRepoService;

    @Override
    public String getInteractionId() {
        return null;
    }

    @Override
    public void handleMessage(ParsedMessage parsedMessage) {
        try {
            ehrRepoService.storeMessage(parsedMessage);
            log.info("Successfully stored small-ehr message");
        } catch (Exception e) {
            log.error("Failed to store small-ehr message", e);
        }
    }
}
