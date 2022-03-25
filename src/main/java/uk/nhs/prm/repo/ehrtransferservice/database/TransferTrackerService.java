package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler.ConversationIdGenerator;
import uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler.TransferTrackerDbEntry;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@AllArgsConstructor
@Service
public class TransferTrackerService {

    private final TransferTrackerDb transferTrackerDb;
    private final ConversationIdGenerator conversationIdGenerator;

    public void recordEventInDb(RepoIncomingEvent incomingEvent) {
        String conversationId = conversationIdGenerator.getConversationId();
        TransferTrackerDbEntry transferTrackerDbEntry =
                new TransferTrackerDbEntry(conversationId, incomingEvent.nhsNumber(), incomingEvent.sourceGP(), incomingEvent.nemsMessageId(), "ACTION:TRANSFER_TO_REPO_STARTED", getTimeNow());
        transferTrackerDb.save(transferTrackerDbEntry);
    }
    private String getTimeNow() {
        return ZonedDateTime.now(ZoneOffset.ofHours(0)).toString();
    }
}
