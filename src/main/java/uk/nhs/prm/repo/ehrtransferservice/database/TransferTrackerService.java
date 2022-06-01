package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferTrackerDbException;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@AllArgsConstructor
@Service
@Slf4j
public class TransferTrackerService {

    private final TransferTrackerDb transferTrackerDb;

    public void createEhrTransfer(RepoIncomingEvent incomingEvent, String status) {
        try {
            TransferTrackerDbEntry transferTrackerDbEntry =
                    new TransferTrackerDbEntry(incomingEvent.getConversationId(), incomingEvent.getNhsNumber(), incomingEvent.getSourceGp(), incomingEvent.getNemsMessageId(), status, getTimeNow());
            transferTrackerDb.save(transferTrackerDbEntry);
            log.info("Recorded initial Repo Incoming event in Transfer Tracker DB with status: " + status);
        } catch (Exception e) {
            log.error("Error encountered while recording Repo Incoming event in Transfer tracker db: " + e.getMessage());
            throw new TransferTrackerDbException("Error encountered while recording Repo Incoming event in Transfer tracker db", e);
        }
    }

    public void updateStateOfEhrTransfer(String conversationId, String status) {
        try {
            transferTrackerDb.update(conversationId, status, getTimeNow());
            log.info("Updated state of EHR transfer in DB to: " + status);
        } catch (Exception e) {
            log.error("Failed to update state of EHR Transfer: " + e.getMessage());
            throw new TransferTrackerDbException("Failed to update state of EHR Transfer: ", e);
        }
    }

    public TransferTrackerDbEntry getEhrTransferData(String conversationId) {
        var ehrData = transferTrackerDb.getByConversationId(conversationId);
        if (ehrData == null) {
            log.error("Failed to retrieve EHR transfer data for conversation ID: " + conversationId);
            throw new TransferTrackerDbException("No entry found in transfer tracker db for conversation Id: " + conversationId);
        }
        return ehrData;
    }

    private String getTimeNow() {
        return ZonedDateTime.now(ZoneOffset.ofHours(0)).toString();
    }
}
