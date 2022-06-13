package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;

@Service
@Slf4j
public class LargeMessageFragmentHandler implements MessageHandler<LargeSqsMessage> {

    private final EhrRepoService ehrRepoService;

    public LargeMessageFragmentHandler(EhrRepoService ehrRepoService) {
        this.ehrRepoService = ehrRepoService;
    }

    @Override
    public String getInteractionId() {
        return null;
    }

    @Override
    public void handleMessage(LargeSqsMessage largeSqsMessage) throws Exception {
        ehrRepoService.storeMessage(largeSqsMessage);
    }
}