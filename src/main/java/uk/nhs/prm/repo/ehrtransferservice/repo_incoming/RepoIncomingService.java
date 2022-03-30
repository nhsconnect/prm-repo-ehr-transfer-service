package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepoIncomingService {

    private final TransferTrackerService transferTrackerService;
    private static final String TRANSFER_TO_REPO_STARTED = "ACTION:TRANSFER_TO_REPO_STARTED";

    public void processIncomingEvent(RepoIncomingEvent repoIncomingEvent) {
        transferTrackerService.recordEventInDb(repoIncomingEvent, TRANSFER_TO_REPO_STARTED);
    }
}
