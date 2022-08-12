package uk.nhs.prm.repo.ehrtransferservice.timeout;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerDb;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.TransferCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.TransferCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class EhrRequestTimeoutHandler {
    TransferTrackerDb transferTrackerDb;
    TransferCompleteMessagePublisher transferCompleteMessagePublisher;

    @Value("${timeOutDurationInHours}")
    String timeout;


    @Scheduled(fixedRateString = "${timeOutFixedScheduleInMilliseconds}")
    public void handle() {
        var timedOutRecords = transferTrackerDb.getTimedOutRecords(getTimeOutTimeStamp());
        timedOutRecords.forEach(record -> {
            updateAllTimeOutRecordsInDb(record.getConversationId());
            sendMessageToTransferCompleteQueue(record);
        });
    }

    private String getTimeOutTimeStamp() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.ofHours(0));

        return now.minus(Integer.valueOf(timeout), ChronoUnit.HOURS).toString();
    }

    private void sendMessageToTransferCompleteQueue(TransferTrackerDbEntry record) {
        transferCompleteMessagePublisher.sendMessage(new TransferCompleteEvent(
                record.getNemsEventLastUpdated(),
                record.getSourceGP(),
                "SUSPENSION",
                record.getNemsMessageId(),
                record.getNhsNumber()),
                UUID.fromString(record.getConversationId()));
    }

    private void updateAllTimeOutRecordsInDb(String conversationId) {
        transferTrackerDb.update(
                conversationId,
                "ACTION:EHR_TRANSFER_TIMEOUT",
                ZonedDateTime.now(ZoneOffset.ofHours(0)).toString(),
                false);
    }
}
