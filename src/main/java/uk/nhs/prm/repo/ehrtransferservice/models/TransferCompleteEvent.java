package uk.nhs.prm.repo.ehrtransferservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferCompleteEvent {
    private String lastUpdated;
    private String previousOdsCode;
    private String eventType;
    private String nemsMessageId;
    private String nhsNumber;
}
