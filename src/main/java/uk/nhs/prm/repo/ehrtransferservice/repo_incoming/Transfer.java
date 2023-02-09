package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import lombok.Data;

@Data
public class Transfer {
    String conversationId;
    String nhsNumber;
    String sourceGP;
    String nemsMessageId;
    String nemsEventLastUpdated;
    String state;
    String createdAt;
    String lastUpdatedAt;
    String largeEhrCoreMessageId;
    Boolean isActive;

    public Transfer(String conversationId, String nhsNumber, String sourceGP, String nemsMessageId, String nemsEventLastUpdated, String state, String createdAt, String lastUpdatedAt, String largeEhrCoreMessageId, Boolean isActive) {
        this.conversationId = conversationId;
        this.nhsNumber = nhsNumber;
        this.sourceGP = sourceGP;
        this.nemsMessageId = nemsMessageId;
        this.nemsEventLastUpdated = nemsEventLastUpdated;
        this.state = state;
        this.createdAt = createdAt;
        this.lastUpdatedAt = lastUpdatedAt;
        this.largeEhrCoreMessageId = largeEhrCoreMessageId;
        this.isActive = isActive;
    }
}
