package uk.nhs.prm.repo.ehrtransferservice.json_models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
public class EhrCompleteEvent {
    public UUID conversationId;
    public UUID messageId;

    public EhrCompleteEvent(UUID conversationId, UUID messageId) {
        this.conversationId = conversationId;
        this.messageId = messageId;
    }
}
