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
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import uk.nhs.prm.repo.ehrtransferservice.config.AppConfig;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.database.model.MessageRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.QueryReturnedNoItemsException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferRecordNotPresentException;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_FAILED;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_STARTED;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.TransferTableAttribute.*;
import static uk.nhs.prm.repo.ehrtransferservice.utility.DateUtility.getIsoTimestamp;

@Component
@RequiredArgsConstructor
public class TransferRepository {
    private final AppConfig config;
    private final DynamoDbClient dynamoDbClient;
    private static final String CONVERSATION_LAYER = "CONVERSATION";
    private static final String CORE_LAYER = "CORE";

    void createConversation(RepoIncomingEvent event) {
        final Map<String, AttributeValue> tableItem = new HashMap<>();
        final String timestamp = getIsoTimestamp();

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
            .s(INBOUND_STARTED.name())
            .build());

        tableItem.put(NEMS_MESSAGE_ID.name, AttributeValue.builder()
            .s(event.getNemsMessageId())
            .build());

        tableItem.put(CREATED_AT.name, AttributeValue.builder()
            .s(timestamp)
            .build());

        tableItem.put(UPDATED_AT.name, AttributeValue.builder()
            .s(timestamp)
            .build());

        final PutItemRequest itemRequest = PutItemRequest.builder()
            .tableName(config.transferTrackerDbTableName())
            .item(tableItem)
            .build();

        dynamoDbClient.putItem(itemRequest);
    }

    boolean isInboundConversationPresent(UUID inboundConversationId) {
        final Map<String, AttributeValue> keyItems = new HashMap<>();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
                .s(inboundConversationId.toString())
                .build());

        keyItems.put(LAYER.name, AttributeValue.builder()
            .s(CONVERSATION_LAYER)
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

    void updateConversationStatus(UUID inboundConversationId, ConversationTransferStatus conversationTransferStatus) {
        final Map<String, AttributeValue> keyItems = new HashMap<>();
        final String updateTimestamp = getIsoTimestamp();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(inboundConversationId.toString())
            .build());

        keyItems.put(LAYER.name, AttributeValue.builder()
            .s(CONVERSATION_LAYER)
            .build());

        final Map<String, AttributeValueUpdate> updateItems = new HashMap<>();

        updateItems.put(TRANSFER_STATUS.name, AttributeValueUpdate.builder()
            .value(AttributeValue.builder().s(conversationTransferStatus.name()).build())
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

    void updateConversationStatusWithFailure(UUID inboundConversationId, String failureCode) {
        final Map<String, AttributeValue> keyItems = new HashMap<>();
        final String updateTimestamp = getIsoTimestamp();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
                .s(inboundConversationId.toString())
                .build());

        keyItems.put(LAYER.name, AttributeValue.builder()
                .s(CONVERSATION_LAYER)
                .build());

        final Map<String, AttributeValueUpdate> updateItems = new HashMap<>();

        updateItems.put(TRANSFER_STATUS.name, AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(INBOUND_FAILED.name()).build())
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

    UUID getEhrCoreInboundMessageIdForInboundConversationId(UUID inboundConversationId) {
        final QueryRequest queryRequest = QueryRequest.builder()
            .tableName(config.transferTrackerDbTableName())
            .keyConditionExpression("%s = :inboundConversationId AND begins_with(%s, :layer)".formatted(
                INBOUND_CONVERSATION_ID.name,
                LAYER.name
            ))
            .expressionAttributeValues(Map.of(
                ":inboundConversationId", AttributeValue.builder()
                    .s(inboundConversationId.toString())
                    .build(),
                ":layer", AttributeValue.builder()
                    .s(CORE_LAYER)
                    .build()
            ))
            .build();

        final QueryResponse queryResponse = dynamoDbClient.query(queryRequest);

        if(!queryResponse.hasItems() || queryResponse.items().isEmpty()) {
            throw new QueryReturnedNoItemsException(inboundConversationId);
        }

        return UUID.fromString(queryResponse.items().get(0).get(INBOUND_MESSAGE_ID.name).s());
    }

    private ConversationRecord mapGetItemResponseToConversationRecord(GetItemResponse itemResponse) {
        Map<String, AttributeValue> item = itemResponse.item();

        return new ConversationRecord(
            UUID.fromString(item.get(INBOUND_CONVERSATION_ID.name).s()),
            Optional.ofNullable(item.get(OUTBOUND_CONVERSATION_ID.name))
                .map(AttributeValue::s)
                .map(UUID::fromString),
            item.get(NHS_NUMBER.name).s(),
            item.get(SOURCE_GP.name).s(),
            Optional.ofNullable(item.get(DESTINATION_GP.name))
                .map(AttributeValue::s),
            item.get(TRANSFER_STATUS.name).s(),
            Optional.ofNullable(item.get(FAILURE_CODE.name))
                .map(AttributeValue::s),
            Optional.ofNullable(item.get(NEMS_MESSAGE_ID.name))
                .map(AttributeValue::s)
                .map(UUID::fromString),
            ZonedDateTime.parse(item.get(CREATED_AT.name).s()),
            ZonedDateTime.parse(item.get(UPDATED_AT.name).s()),
            Optional.ofNullable(item.get(DELETED_AT.name))
                .map(AttributeValue::s)
                .map(Instant::parse)
        );
    }

    private MessageRecord mapGetItemResponseToMessageRecord(GetItemResponse itemResponse) {
        Map<String, AttributeValue> item = itemResponse.item();

        return new MessageRecord(
            UUID.fromString(item.get(INBOUND_CONVERSATION_ID.name).s()),
            Optional.ofNullable(item.get(OUTBOUND_CONVERSATION_ID.name))
                .map(AttributeValue::s)
                .map(UUID::fromString),
            Optional.ofNullable(item.get(TRANSFER_STATUS.name))
                .map(AttributeValue::s),
            LocalDateTime.parse(item.get(CREATED_AT.name).s()),
            LocalDateTime.parse(item.get(UPDATED_AT.name).s()),
            Optional.ofNullable(item.get(DELETED_AT.name))
                .map(AttributeValue::s)
                .map(Instant::parse)
        );
    }
}