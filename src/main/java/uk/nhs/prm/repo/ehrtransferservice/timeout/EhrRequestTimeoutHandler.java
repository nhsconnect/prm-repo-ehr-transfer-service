package uk.nhs.prm.repo.ehrtransferservice.timeout;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerDb;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.TransferCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.TransferCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
public class EhrRequestTimeoutHandler {
    TransferTrackerDb transferTrackerDb;
    TransferCompleteMessagePublisher transferCompleteMessagePublisher;

    @Value("${timeOutDurationInHours}")
    String timeout;


    @Scheduled(fixedRateString = "${timeOutFixedScheduleInMilliseconds}")
    public void handle() {
        log.info("Running schedule job to check for timed-out records");
        log.info("timeout duration in hours is : {}", timeout);
        var timedOutRecords = transferTrackerDb.getTimedOutRecords(getTimeOutTimeStamp());
        log.info("Number of timed-out records are: {}", timedOutRecords.size() );
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
        log.info("Sending message to transfer complete queue for timed-out records");
        transferCompleteMessagePublisher.sendMessage(new TransferCompleteEvent(
                record.getNemsEventLastUpdated(),
                record.getSourceGP(),
                "SUSPENSION",
                record.getNemsMessageId(),
                record.getNhsNumber()),
                UUID.fromString(record.getConversationId()));
    }

    private void updateAllTimeOutRecordsInDb(String conversationId) {
        log.info("Updating transfer tracker db with state : {}", "ACTION:EHR_TRANSFER_TIMEOUT");
        transferTrackerDb.update(
                conversationId,
                "ACTION:EHR_TRANSFER_TIMEOUT",
                ZonedDateTime.now(ZoneOffset.ofHours(0)).toString(),
                false);
    }
}
