package uk.nhs.prm.repo.ehrtransferservice.database;

import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;

import java.util.UUID;

@Component
public class TransferRepository {
    public void createConversation() {
        throw new UnsupportedOperationException();
    }

    public ConversationRecord findConversation() {
        throw new UnsupportedOperationException();
    }

    public boolean isInboundConversationIdPresent(UUID inboundConversationId) {
        throw new UnsupportedOperationException();
    }

    public String getConversationStatus(UUID inboundConversationId) {
        throw new UnsupportedOperationException();
    }

    public void updateConversationStatus(UUID inboundConversationId, String status) {
        throw new UnsupportedOperationException();
    }
}
