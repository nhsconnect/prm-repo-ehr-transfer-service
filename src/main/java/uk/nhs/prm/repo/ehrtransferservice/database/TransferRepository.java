package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import uk.nhs.prm.repo.ehrtransferservice.config.AppConfig;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.TransferState.EHR_TRANSFER_STARTED;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.CREATED_AT;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.DESTINATION_GP;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.INBOUND_CONVERSATION_ID;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.LAYER;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.NEMS_MESSAGE_ID;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.NHS_NUMBER;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.SOURCE_GP;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.STATE;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.UPDATED_AT;

@Component
@RequiredArgsConstructor
class TransferRepository {
    private final DynamoDbClient client;
    private final AppConfig config;
    private static final String CONVERSATION_LAYER = "CONVERSATION";
    private static final String CORE_LAYER = "CORE";
    private static final String FRAGMENT_LAYER = "FRAGMENT#%s";

    void createConversation(RepoIncomingEvent event) {
        final Map<String, AttributeValue> tableItem = new HashMap<>();
        final String creationTimestamp = LocalDateTime.now().toString();

        tableItem.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(event.getConversationId())
            .build());

        tableItem.put(LAYER.name, AttributeValue.builder()
            .s(CONVERSATION_LAYER).build());

        tableItem.put(NHS_NUMBER.name, AttributeValue.builder()
            .s(event.getNhsNumber())
            .build());

        tableItem.put(SOURCE_GP.name, AttributeValue.builder()
            .s(event.getSourceGp())
            .build());

        tableItem.put(DESTINATION_GP.name, AttributeValue.builder()
            .s(event.getDestinationGp())
            .build());

        tableItem.put(STATE.name, AttributeValue.builder()
            .s(EHR_TRANSFER_STARTED.name())
            .build());

        tableItem.put(NEMS_MESSAGE_ID.name, AttributeValue.builder()
            .s(event.getNemsMessageId())
            .build());

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

    GetItemResponse findConversationByInboundConversationId(UUID inboundConversationId) {
        final Map<String, AttributeValue> keyItems = new HashMap<>();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(inboundConversationId.toString())
            .build());

        final GetItemRequest itemRequest = GetItemRequest.builder()
            .tableName(this.config.transferTrackerDbTableName())
            .key(keyItems)
            .build();

        return this.client.getItem(itemRequest);
    }

    boolean isInboundConversationIdPresent(UUID inboundConversationId) {
        throw new UnsupportedOperationException();
    }

    void updateConversationStatus(UUID inboundConversationId, TransferState status, String layer) {
        final Map<String, AttributeValue> keyItems = new HashMap<>();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(inboundConversationId.toString())
            .build());

        keyItems.put(LAYER.name, AttributeValue.builder()
            .s(CONVERSATION_LAYER)
            .build());

        final UpdateItemRequest itemRequest = UpdateItemRequest.builder()
            .tableName(this.config.transferTrackerDbTableName())
            .key(keyItems)
            .attributeUpdates(Map.of(STATE.name, AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(status.name()).build())
                .action(AttributeAction.PUT)
                .build()))
            .build();

        this.client.updateItem(itemRequest);
    }
}
