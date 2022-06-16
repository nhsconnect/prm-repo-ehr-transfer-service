package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeMessageFragments;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;

@Service
@Slf4j
public class LargeMessageFragmentHandler implements MessageHandler<LargeSqsMessage> {

    private final EhrRepoService ehrRepoService;
    private final TransferTrackerService transferTrackerService;

    public LargeMessageFragmentHandler(EhrRepoService ehrRepoService, TransferTrackerService transferTrackerService) {
        this.ehrRepoService = ehrRepoService;
        this.transferTrackerService = transferTrackerService;
    }

    @Override
    public String getInteractionId() {
        return null;
    }

    @Override
    public void handleMessage(LargeSqsMessage largeSqsMessage) throws Exception {
        //var transferTrackerTrData = transferTrackerService.getEhrTransferData(largeSqsMessage.getConversationId().toString());
        var largeMessageFragments = new LargeMessageFragments(largeSqsMessage);
        ehrRepoService.storeMessage(largeMessageFragments);
    }
}