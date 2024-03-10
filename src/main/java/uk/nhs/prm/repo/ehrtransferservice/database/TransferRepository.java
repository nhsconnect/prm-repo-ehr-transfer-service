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
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.database.model.MessageRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferRecordNotPresentException;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.TransferStatus.EHR_TRANSFER_STARTED;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.CREATED_AT;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.DELETED_AT;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.DESTINATION_GP;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.FAILURE_CODE;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.INBOUND_CONVERSATION_ID;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.LAYER;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.MESSAGE_ID;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.NEMS_MESSAGE_ID;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.NHS_NUMBER;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.SOURCE_GP;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.TRANSFER_STATUS;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.UPDATED_AT;

@Component
@RequiredArgsConstructor
public class TransferRepository {
    private final AppConfig config;
    private final DynamoDbClient dynamoDbClient;

    private static final String CONVERSATION_LAYER = "CONVERSATION";
    private static final String CORE_LAYER = "CORE";

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

        tableItem.put(TRANSFER_STATUS.name, AttributeValue.builder()
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

        dynamoDbClient.putItem(itemRequest);
    }

    void createCore(UUID inboundConversationId, UUID coreMessageId) {
        final Map<String, AttributeValue> tableItem = new HashMap<>();
        final String creationTimestamp = LocalDateTime.now().toString();

        tableItem.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(inboundConversationId.toString())
            .build());

        tableItem.put(LAYER.name, AttributeValue.builder()
            .s(CORE_LAYER).build());

        tableItem.put(MESSAGE_ID.name, AttributeValue.builder()
            .s(coreMessageId.toString())
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

        dynamoDbClient.putItem(itemRequest);
    }

    boolean isInboundConversationIdPresent(UUID inboundConversationId) {
        final Map<String, AttributeValue> keyItems = new HashMap<>();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
                .s(inboundConversationId.toString())
                .build());

        final GetItemRequest itemRequest = GetItemRequest.builder()
                .tableName(config.transferTrackerDbTableName())
                .key(keyItems)
                .build();

        return dynamoDbClient
                .getItem(itemRequest)
                .hasItem();
    }

    ConversationRecord findConversationByInboundConversationId(UUID inboundConversationId) {
        final Map<String, AttributeValue> keyItem = new HashMap<>();

        keyItem.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(inboundConversationId.toString())
            .build());

        keyItem.put(LAYER.name, AttributeValue.builder()
            .s(CONVERSATION_LAYER)
            .build());

        final GetItemRequest itemRequest = GetItemRequest.builder()
            .tableName(config.transferTrackerDbTableName())
            .key(keyItem)
            .build();

        GetItemResponse response = dynamoDbClient.getItem(itemRequest);

        if (!response.hasItem()) {
            throw new TransferRecordNotPresentException(inboundConversationId);
        }

        return mapGetItemResponseToConversationRecord(response);
    }

    void updateConversationStatus(UUID inboundConversationId, TransferStatus state) {
        final Map<String, AttributeValue> keyItems = new HashMap<>();
        final String updateTimestamp = LocalDateTime.now().toString();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(inboundConversationId.toString())
            .build());

        keyItems.put(LAYER.name, AttributeValue.builder()
            .s(CONVERSATION_LAYER)
            .build());

        final Map<String, AttributeValueUpdate> updateItems = new HashMap<>();

        updateItems.put(TRANSFER_STATUS.name, AttributeValueUpdate.builder()
            .value(AttributeValue.builder().s(state.name()).build())
            .action(AttributeAction.PUT)
            .build());

        updateItems.put(UPDATED_AT.name, AttributeValueUpdate.builder()
            .value(AttributeValue.builder().s(updateTimestamp).build())
            .action(AttributeAction.PUT)
            .build());

        final UpdateItemRequest itemRequest = UpdateItemRequest.builder()
            .tableName(config.transferTrackerDbTableName())
            .key(keyItems)
            .attributeUpdates(updateItems)
            .build();

        dynamoDbClient.updateItem(itemRequest);
    }

    void updateConversationStatusWithFailure(UUID inboundConversationId, TransferStatus state, String failureCode) {
        final Map<String, AttributeValue> keyItems = new HashMap<>();
        final String updateTimestamp = LocalDateTime.now().toString();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
                .s(inboundConversationId.toString())
                .build());

        keyItems.put(LAYER.name, AttributeValue.builder()
                .s(CONVERSATION_LAYER)
                .build());

        final Map<String, AttributeValueUpdate> updateItems = new HashMap<>();

        updateItems.put(TRANSFER_STATUS.name, AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(state.name()).build())
                .action(AttributeAction.PUT)
                .build());

        updateItems.put(FAILURE_CODE.name, AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(failureCode).build())
                .action(AttributeAction.PUT)
                .build());

        updateItems.put(UPDATED_AT.name, AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(updateTimestamp).build())
                .action(AttributeAction.PUT)
                .build());

        final UpdateItemRequest itemRequest = UpdateItemRequest.builder()
                .tableName(config.transferTrackerDbTableName())
                .key(keyItems)
                .attributeUpdates(updateItems)
                .build();

        dynamoDbClient.updateItem(itemRequest);
    }

    MessageRecord findCoreByInboundConversationId(UUID inboundConversationId) {
        final Map<String, AttributeValue> keyItems = new HashMap<>();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(inboundConversationId.toString())
            .build());

        keyItems.put(LAYER.name, AttributeValue.builder()
            .s(CORE_LAYER)
            .build());

        final GetItemRequest itemRequest = GetItemRequest.builder()
            .tableName(config.transferTrackerDbTableName())
            .key(keyItems)
            .build();

        GetItemResponse response = dynamoDbClient.getItem(itemRequest);

        if (!response.hasItem()) {
            throw new TransferRecordNotPresentException(inboundConversationId);
        }

        return mapGetItemResponseToMessageRecord(response);
    }

    // Helper Methods
    ConversationRecord mapGetItemResponseToConversationRecord(GetItemResponse itemResponse) {
        Map<String, AttributeValue> item = itemResponse.item();

        return new ConversationRecord(
            UUID.fromString(item.get(INBOUND_CONVERSATION_ID.name).s()),
            item.get(NHS_NUMBER.name).s(),
            item.get(SOURCE_GP.name).s(),
            Optional.ofNullable(item.get(DESTINATION_GP.name))
                .map(AttributeValue::s),
            item.get(TRANSFER_STATUS.name).s(),
            Optional.ofNullable(item.get(FAILURE_CODE.name))
                .map(AttributeValue::s),
            Optional.ofNullable(item.get(NEMS_MESSAGE_ID.name))
                .map(attributeValue -> UUID.fromString(attributeValue.s())),
            // TODO PRMT-4524 this assumes dates are stored as a string, may need to refactor!
            LocalDateTime.parse(item.get(CREATED_AT.name).s()),
            LocalDateTime.parse(item.get(UPDATED_AT.name).s())
        );
    }

    MessageRecord mapGetItemResponseToMessageRecord(GetItemResponse itemResponse) {
        Map<String, AttributeValue> item = itemResponse.item();

        return new MessageRecord(
            UUID.fromString(item.get(INBOUND_CONVERSATION_ID.name).s()),
            Optional.ofNullable(item.get(MESSAGE_ID.name)).map(attributeValue ->
                UUID.fromString(attributeValue.s())),
            item.get(TRANSFER_STATUS.name).s(),
            LocalDateTime.parse(item.get(CREATED_AT.name).s()),
            LocalDateTime.parse(item.get(UPDATED_AT.name).s()),
            Optional.ofNullable(item.get(DELETED_AT.name)).map(attributeValue ->
                Instant.parse(attributeValue.s()))
        );
    }
}