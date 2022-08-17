package uk.nhs.prm.repo.ehrtransferservice.timeout;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerDb;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.TransferCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.TransferCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EhrRequestTimeoutHandler {
    private final TransferTrackerDb transferTrackerDb;
    private final Tracer tracer;
    private final TransferCompleteMessagePublisher transferCompleteMessagePublisher;

    @Value("${timeOutDurationInSeconds}")
    String timeoutInSeconds;


    @Scheduled(fixedRateString = "${timeOutFixedScheduleInSeconds}",  timeUnit = TimeUnit.SECONDS)
    public void handle() {
        try {
            log.info("Running schedule job to check for timed-out records");
            log.info("timeout duration in seconds is : {}", timeoutInSeconds);
            var timedOutRecords = transferTrackerDb.getTimedOutRecords(getTimeOutTimeStamp());
            log.info("Number of timed-out records are: {}", timedOutRecords.size());
            timedOutRecords.forEach(record -> {
                tracer.setTraceId(record.getConversationId());
                updateAllTimedOutRecordsInDb(record.getConversationId());
                sendMessageToTransferCompleteQueue(record);
            });
        } catch (Exception e) {
            log.error("Encountered exception with handling timeouts ", e);
        }
    }

    private String getTimeOutTimeStamp() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.ofHours(0));
        return now.minus(Integer.valueOf(timeoutInSeconds), ChronoUnit.SECONDS).toString();
    }

    private void sendMessageToTransferCompleteQueue(TransferTrackerDbEntry record) {
        log.info("Sending message with conversationId : {} to transfer complete queue for timed-out records", record.getConversationId());
        transferCompleteMessagePublisher.sendMessage(new TransferCompleteEvent(
                        record.getNemsEventLastUpdated(),
                        record.getSourceGP(),
                        "SUSPENSION",
                        record.getNemsMessageId(),
                        record.getNhsNumber()),
                UUID.fromString(record.getConversationId()));
    }

    private void updateAllTimedOutRecordsInDb(String conversationId) {
        log.info("Updating transfer tracker db for conversationId : {} with state : {}",conversationId, "ACTION:EHR_TRANSFER_TIMEOUT");
        transferTrackerDb.update(
                conversationId,
                "ACTION:EHR_TRANSFER_TIMEOUT",
                ZonedDateTime.now(ZoneOffset.ofHours(0)).toString(),
                false);
    }
}
