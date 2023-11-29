package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
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
    boolean isActive;
}
