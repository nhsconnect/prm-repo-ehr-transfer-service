package uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerDb;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EhrRequestService {

    TransferTrackerService transferTrackerService;
    RepoIncomingEventParser incomingEventParser;

    public void processIncomingEvent(String incomingEvent) {
        RepoIncomingEvent repoIncomingEvent = parseMessage(incomingEvent);
        transferTrackerService.recordEventInDb(repoIncomingEvent);
    }

    private RepoIncomingEvent parseMessage(String incomingEvent) {
        try {
            return incomingEventParser.parse(incomingEvent);
        } catch (JsonProcessingException e) {
            log.error("Encountered Exception while trying to parse incoming event message");
            throw new RuntimeException(e);
        }
    }

}
