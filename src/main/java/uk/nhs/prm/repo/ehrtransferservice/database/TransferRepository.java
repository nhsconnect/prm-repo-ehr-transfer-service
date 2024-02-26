package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import uk.nhs.prm.repo.ehrtransferservice.config.AppConfig;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.*;

@Component
@RequiredArgsConstructor
public class TransferRepository {
    private final DynamoDbClient client;
    private final AppConfig config;
    private static final String CONVERSATION_LAYER = "CONVERSATION";
    private static final String CORE_LAYER = "CORE";
    private static final String FRAGMENT_LAYER = "FRAGMENT#%s";

    public void createConversation(ConversationRecord record) {
        final Map<String, AttributeValue> tableItem = new HashMap<>();
        final String creationTimestamp = LocalDateTime.now().toString();

        tableItem.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(record.inboundConversationId().toString())
            .build());

        tableItem.put(LAYER.name, AttributeValue.builder()
            .s(CONVERSATION_LAYER).build());

        if(record.outboundConversationId().isPresent()) {
            tableItem.put(OUTBOUND_CONVERSATION_ID.name, AttributeValue.builder()
                .s(record.outboundConversationId().get().toString())
                .build());
        }

        if(record.nhsNumber().isPresent()) {
            tableItem.put(NHS_NUMBER.name, AttributeValue.builder()
                .s(record.nhsNumber().get())
                .build());
        }

        tableItem.put(SOURCE_GP.name, AttributeValue.builder()
            .s(record.sourceGp())
            .build());

        if(record.destinationGp().isPresent()) {
            tableItem.put(DESTINATION_GP.name, AttributeValue.builder()
                .s(record.destinationGp().get())
                .build());
        }

        tableItem.put(STATE.name, AttributeValue.builder()
            .s(record.state())
            .build());

        if(record.meshMessageId().isPresent()) {
            tableItem.put(MESH_MESSAGE_ID.name, AttributeValue.builder()
                .s(record.meshMessageId().get().toString())
                .build());
        }

        if(record.nemsMessageId().isPresent()) {
            tableItem.put(NEMS_MESSAGE_ID.name, AttributeValue.builder()
                .s(record.nemsMessageId().get().toString())
                .build());
        }

        tableItem.put(CREATED_AT.name, AttributeValue.builder()
            .s(creationTimestamp)
            .build());

        tableItem.put(UPDATED_AT.name, AttributeValue.builder()
            .s(creationTimestamp)
            .build());

        final PutItemRequest itemRequest = PutItemRequest.builder()
            .tableName(config.transferTrackerDbTableName())
            .item(tableItem)
            .build();

        this.client.putItem(itemRequest);
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
