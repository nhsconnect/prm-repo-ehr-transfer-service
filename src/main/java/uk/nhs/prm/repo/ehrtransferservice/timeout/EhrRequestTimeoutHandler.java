package uk.nhs.prm.repo.ehrtransferservice.timeout;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerDb;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class EhrRequestTimeoutHandler {
    TransferTrackerDb transferTrackerDb;

    @Value("${timeOutDurationInHours}")
    String timeout;


    @Scheduled()
    public void handle() {
        List<String> conversationIds = getConversationIdsForTimedOutRecords(getTimeOutTimeStamp());
        updateAllTimeOutRecordsInDb(conversationIds);
        sendMessageToTransferCompleteQueue();

    }

    private String getTimeOutTimeStamp() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.ofHours(0));

        return now.minus(Integer.valueOf(timeout), ChronoUnit.HOURS).toString();
    }

    private void sendMessageToTransferCompleteQueue() {
    }

    private void updateAllTimeOutRecordsInDb(List<String> conversationIds) {
        conversationIds.forEach(conversationId -> transferTrackerDb.update(conversationId
                ,"ACTION:EHR_TRANSFER_TIMEOUT"
                , ZonedDateTime.now(ZoneOffset.ofHours(0)).toString()
                ,false));
    }

    private List<String> getConversationIdsForTimedOutRecords(String timeout) {
        List<String> conversationIds = new ArrayList<>();
        List<TransferTrackerDbEntry> timedOutRecords = transferTrackerDb.getTimedOutRecords(timeout);
        timedOutRecords.forEach(record -> conversationIds.add(record.getConversationId()));
        return conversationIds;
    }
}
