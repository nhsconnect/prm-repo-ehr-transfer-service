package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.ConversationIdStore;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@AllArgsConstructor
@Service
@Slf4j
public class TransferTrackerService {

    private final TransferTrackerDb transferTrackerDb;
    private final ConversationIdStore conversationIdStore;

    public void recordEventInDb(RepoIncomingEvent incomingEvent, String status) {
        try {
            log.info("Recording an event in transfer tracker db with status : " + status);
            TransferTrackerDbEntry transferTrackerDbEntry =
                    new TransferTrackerDbEntry(conversationIdStore.getConversationId(), incomingEvent.getNhsNumber(), incomingEvent.getSourceGp(), incomingEvent.getNemsMessageId(), status, getTimeNow());
            transferTrackerDb.save(transferTrackerDbEntry);
        } catch (Exception e) {
            log.error("Error encountered while recording event in transfer tracker db" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    private String getTimeNow() {
        return ZonedDateTime.now(ZoneOffset.ofHours(0)).toString();
    }
}