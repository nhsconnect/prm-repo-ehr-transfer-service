package uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler;

import lombok.Data;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Data
public class TransferTrackerDbEntry {
    String conversationId;
    String nhsNumber;
    String sourceGP;
    String nemsMessageId;
    String state;
    String dateTime;

    public TransferTrackerDbEntry(String conversationId, String nhsNumber, String sourceGP, String nemsMessageId, String state, String dateTime) {
        this.conversationId = conversationId;
        this.nhsNumber = nhsNumber;
        this.sourceGP = sourceGP;
        this.nemsMessageId = nemsMessageId;
        this.state = state;
        this.dateTime = dateTime;
    }
}
