package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import lombok.Data;

@Data
public class TransferTrackerDbEntry {
    String conversationId;
    String nhsNumber;
    String sourceGP;
    String nemsMessageId;
    String nemsEventLastUpdated;
    String state;
    String dateTime;
    String largeEhrCoreMessageId;
    Boolean isActive;

    public TransferTrackerDbEntry(String conversationId, String nhsNumber, String sourceGP, String nemsMessageId, String nemsEventLastUpdated, String state, String dateTime, String largeEhrCoreMessageId, Boolean isActive) {
        this.conversationId = conversationId;
        this.nhsNumber = nhsNumber;
        this.sourceGP = sourceGP;
        this.nemsMessageId = nemsMessageId;
        this.nemsEventLastUpdated = nemsEventLastUpdated;
        this.state = state;
        this.dateTime = dateTime;
        this.largeEhrCoreMessageId = largeEhrCoreMessageId;
        this.isActive = isActive;
    }
}
