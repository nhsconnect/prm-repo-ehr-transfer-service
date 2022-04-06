package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepoIncomingService {

    private static final String TRANSFER_TO_REPO_STARTED = "ACTION:TRANSFER_TO_REPO_STARTED";
    private static final String EHR_REQUEST_SENT = "ACTION:EHR_REQUEST_SENT";

    private final TransferTrackerService transferTrackerService;
    private final Gp2gpMessengerService gp2gpMessengerService;

    public void processIncomingEvent(RepoIncomingEvent repoIncomingEvent) throws Exception {
        transferTrackerService.createEhrTransfer(repoIncomingEvent, TRANSFER_TO_REPO_STARTED);
        gp2gpMessengerService.sendEhrRequest(repoIncomingEvent);
        transferTrackerService.updateStateOfEhrTransfer(repoIncomingEvent.getConversationId(), EHR_REQUEST_SENT);
    }
}
