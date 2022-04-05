package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferTrackerDbException;
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
            TransferTrackerDbEntry transferTrackerDbEntry =
                    new TransferTrackerDbEntry(conversationIdStore.getConversationId(), incomingEvent.getNhsNumber(), incomingEvent.getSourceGp(), incomingEvent.getNemsMessageId(), status, getTimeNow());
            transferTrackerDb.save(transferTrackerDbEntry);
            log.info("Recorded initial Repo Incoming event in Transfer Tracker DB with status: " + status);
        } catch (Exception e) {
            log.error("Error encountered while recording Repo Incoming event in Transfer tracker db" + e.getMessage());
            throw new TransferTrackerDbException("Error encountered while recording Repo Incoming event in Transfer tracker db", e);
        }
    }

    public void updateStateOfTransfer(String status) {
        try {
            transferTrackerDb.update(conversationIdStore.getConversationId(), status, getTimeNow());
            log.info("Updated state of transfer in DB to: " + status);
        } catch (Exception e) {
            log.error("Failed to update state of EHR Transfer: " + e.getMessage());
            throw new TransferTrackerDbException("Failed to update state of EHR Transfer: ", e);
        }
    }

    private String getTimeNow() {
        return ZonedDateTime.now(ZoneOffset.ofHours(0)).toString();
    }
}
