package uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RepoIncomingEvent {
    private String nhsNumber;
    private String sourceGp;
    private String nemsMessageId;
    private String destinationGp;
}
