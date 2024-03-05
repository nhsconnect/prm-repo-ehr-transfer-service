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
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferRecordNotPresentException;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.TransferState.EHR_TRANSFER_STARTED;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.CREATED_AT;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.DESTINATION_GP;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.FAILURE_CODE;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.INBOUND_CONVERSATION_ID;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.LAYER;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.NEMS_MESSAGE_ID;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.NHS_NUMBER;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.OUTBOUND_CONVERSATION_ID;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.SOURCE_GP;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.STATE;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.UPDATED_AT;

@Component
@RequiredArgsConstructor
class TransferRepository {
    private final DynamoDbClient dynamoDbClient;
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
        final Map<String, AttributeValue> keyItems = new HashMap<>();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(inboundConversationId.toString())
            .build());

        final GetItemRequest itemRequest = GetItemRequest.builder()
            .tableName(config.transferTrackerDbTableName())
            .key(keyItems)
            .build();

        GetItemResponse response = dynamoDbClient.getItem(itemRequest);

        if (!response.hasItem()) {
            throw new TransferRecordNotPresentException(inboundConversationId);
        }

        return mapGetItemResponseToConversationRecord(response);
    }

    void updateConversationStatus(UUID inboundConversationId, TransferState state) {
        final Map<String, AttributeValue> keyItems = new HashMap<>();
        final String updateTimestamp = LocalDateTime.now().toString();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(inboundConversationId.toString())
            .build());

        keyItems.put(LAYER.name, AttributeValue.builder()
            .s(CONVERSATION_LAYER)
            .build());

        final Map<String, AttributeValueUpdate> updateItems = new HashMap<>();

        updateItems.put(STATE.name, AttributeValueUpdate.builder()
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

    void updateConversationStatusWithFailure(UUID inboundConversationId, TransferState state, String failureCode) {
        final Map<String, AttributeValue> keyItems = new HashMap<>();
        final String updateTimestamp = LocalDateTime.now().toString();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
                .s(inboundConversationId.toString())
                .build());

        keyItems.put(LAYER.name, AttributeValue.builder()
                .s(CONVERSATION_LAYER)
                .build());

        final Map<String, AttributeValueUpdate> updateItems = new HashMap<>();

        updateItems.put(STATE.name, AttributeValueUpdate.builder()
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

    ConversationRecord mapGetItemResponseToConversationRecord(GetItemResponse response) {
        Map<String, AttributeValue> item = response.item();

        return new ConversationRecord(
                UUID.fromString(item.get(INBOUND_CONVERSATION_ID.name).s()),
                Optional.ofNullable(item.get(OUTBOUND_CONVERSATION_ID.name))
                        .map(attributeValue -> UUID.fromString(attributeValue.s())),
                Optional.ofNullable(item.get(NHS_NUMBER.name))
                        .map(AttributeValue::s),
                item.get(SOURCE_GP.name).s(),
                Optional.ofNullable(item.get(DESTINATION_GP.name))
                        .map(AttributeValue::s),
                item.get(STATE.name).s(),
                Optional.ofNullable(item.get(FAILURE_CODE.name))
                        .map(AttributeValue::s),
                Optional.ofNullable(item.get(NEMS_MESSAGE_ID.name))
                        .map(attributeValue -> UUID.fromString(attributeValue.s())),
                // TODO PRMT-4524 this assumes dates are stored as a string, may need to refactor!
                LocalDateTime.parse(item.get(CREATED_AT.name).s()),
                LocalDateTime.parse(item.get(UPDATED_AT.name).s())
        );
    }
}