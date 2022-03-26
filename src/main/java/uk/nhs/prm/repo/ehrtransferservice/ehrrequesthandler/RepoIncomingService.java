package uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepoIncomingService {

    private final TransferTrackerService transferTrackerService;

    public void processIncomingEvent(RepoIncomingEvent repoIncomingEvent) {
        transferTrackerService.recordEventInDb(repoIncomingEvent);
    }
}
