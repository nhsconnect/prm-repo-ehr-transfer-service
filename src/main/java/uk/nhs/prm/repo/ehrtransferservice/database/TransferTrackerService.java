package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler.ConversationIdStore;
import uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler.TransferTrackerDbEntry;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@AllArgsConstructor
@Service
@Slf4j
public class TransferTrackerService {

    private final TransferTrackerDb transferTrackerDb;
    private final ConversationIdStore conversationIdStore;

    public void recordEventInDb(RepoIncomingEvent incomingEvent) {
        String conversationId = conversationIdStore.getConversationId();
        try {
            log.info("Recording an event in transfer tracker db with status : " + "ACTION:TRANSFER_TO_REPO_STARTED");
            TransferTrackerDbEntry transferTrackerDbEntry =
                    new TransferTrackerDbEntry(conversationId, incomingEvent.getNhsNumber(), incomingEvent.getSourceGP(), incomingEvent.getNemsMessageId(), "ACTION:TRANSFER_TO_REPO_STARTED", getTimeNow());
            transferTrackerDb.save(transferTrackerDbEntry);
        }catch(Exception e) {
            log.error("Error encountered while recording event in transfer tracker db" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    private String getTimeNow() {
        return ZonedDateTime.now(ZoneOffset.ofHours(0)).toString();
    }
}
