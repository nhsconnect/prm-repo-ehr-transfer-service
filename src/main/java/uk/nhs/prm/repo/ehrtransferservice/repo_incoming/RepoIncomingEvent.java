package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepoIncomingEvent {
    private String nhsNumber;
    private String sourceGp;
    private String nemsMessageId;
    private String destinationGp;
    private String nemsEventLastUpdated;
    private String conversationId;
}
