package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.json_models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EhrCompleteHandler {
    private final Gp2gpMessengerService gp2gpMessengerService;
    private final TransferTrackerService transferTrackerService;

    public void handleMessage(EhrCompleteEvent ehrCompleteEvent) {
        gp2gpMessengerService.sendEhrCompletePositiveAcknowledgement(ehrCompleteEvent, getEhrData(ehrCompleteEvent));
        // update state in transfer to EHR_TRANSFER_TO_REPO_COMPLETE
        // Put EHR complete message on the transfer complete topic
    }

    private TransferTrackerDbEntry getEhrData(EhrCompleteEvent ehrCompleteEvent) {
        var conversationId = ehrCompleteEvent.getConversationId().toString();
        return transferTrackerService.getEhrTransferData(conversationId);
    }
}
