package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferTrackerDbException;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.SplunkAuditPublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.SplunkAuditMessage;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@AllArgsConstructor
@Service
@Slf4j
public class TransferTrackerService {

    private final TransferTrackerDb transferTrackerDb;
    private final SplunkAuditPublisher splunkAuditPublisher;

    public void createEhrTransfer(RepoIncomingEvent incomingEvent, String status) {
        try {
            TransferTrackerDbEntry transferTrackerDbEntry =
                    new TransferTrackerDbEntry(
                            incomingEvent.getConversationId(),
                            incomingEvent.getNhsNumber(),
                            incomingEvent.getSourceGp(),
                            incomingEvent.getNemsMessageId(),
                            incomingEvent.getNemsEventLastUpdated(),
                            status,
                            getTimeNow(),
                            getTimeNow(),
                            getLargeEhrCoreMessageId(),
                            isActive()
                    );
            transferTrackerDb.save(transferTrackerDbEntry);
            log.info("Recorded initial Repo Incoming event in Transfer Tracker DB with status: " + status);
        } catch (Exception e) {
            log.error("Error encountered while recording Repo Incoming event in Transfer tracker db: " + e.getMessage());
            throw new TransferTrackerDbException("Error encountered while recording Repo Incoming event in Transfer tracker db", e);
        }
    }

    private String getLargeEhrCoreMessageId() {
        String largeEhrCoreMessageId = ""; //This is a placeholder for the messageId from the LargeEhrCoreQueue
        return largeEhrCoreMessageId;
    }

    private boolean isActive() {
        return true;
    }

    public void handleEhrTransferStateUpdate(String conversationId, String nemsMessageId, String status) {
        try {
            transferTrackerDb.update(conversationId, status, getTimeNow());
            log.info("Updated state of EHR transfer in DB to: " + status);
            publishAuditMessage(conversationId, nemsMessageId, status);
        } catch (Exception e) {
            log.error("Failed to update state of EHR Transfer: " + e.getMessage());
            throw new TransferTrackerDbException("Failed to update state of EHR Transfer: ", e);
        }
    }

    private void publishAuditMessage(String conversationId, String nemsMessageId, String status) {
        splunkAuditPublisher.sendMessage(new SplunkAuditMessage(conversationId, nemsMessageId, status));
        log.info("Published audit message with the status of: " + status);
    }

    public TransferTrackerDbEntry getEhrTransferData(String conversationId) {
        var ehrData = transferTrackerDb.getByConversationId(conversationId);
        if (ehrData == null) {
            log.error("Failed to retrieve EHR transfer data for conversation ID: " + conversationId);
            throw new TransferTrackerDbException("No entry found in transfer tracker db for conversation Id: " + conversationId);
        }
        return ehrData;
    }

    public void updateLargeEhrCoreMessageId(String conversationId, String messageId) {
        try {
            transferTrackerDb.updateLargeEhrCoreMessageId(conversationId, messageId);
            log.info("Updated large EHR core message ID in DB");
        } catch (Exception e) {
            log.error("Failed to update large EHR core message ID in DB: " + e.getMessage());
            throw new TransferTrackerDbException("Failed to update large EHR core message ID in DB: ", e);
        }
    }

    private String getTimeNow() {
        return ZonedDateTime.now(ZoneOffset.ofHours(0)).toString();
    }
}
