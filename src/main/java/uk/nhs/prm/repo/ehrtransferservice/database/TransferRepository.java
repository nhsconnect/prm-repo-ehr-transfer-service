package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import uk.nhs.prm.repo.ehrtransferservice.config.AppConfig;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransferRepository {
    private final DynamoDbClient client;
    private final AppConfig config;

    public void createConversation(ConversationRecord record) {
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

    public void updateConversationStatus(UUID inboundConversationId, TransferStatus status) {
        throw new UnsupportedOperationException();
    }
}
