package uk.nhs.prm.repo.ehrtransferservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
public class SplunkAuditMessage {
    private String conversationId;
    private String nemsMessageId;
    private String status;
}
