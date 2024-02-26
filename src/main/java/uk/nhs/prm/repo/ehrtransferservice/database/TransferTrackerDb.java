package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import uk.nhs.prm.repo.ehrtransferservice.config.AppConfig;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.Transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
@Component
@RequiredArgsConstructor
@Slf4j
public class TransferTrackerDb {
    private final DynamoDbClient dynamoDbClient;
    private final AppConfig config;

    public Transfer getByConversationId(String conversationId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("conversation_id", AttributeValue.builder().s(conversationId).build());
        var getItemResponse = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(config.transferTrackerDbTableName())
                .key(key)
                .build());

        if (!getItemResponse.hasItem()) {
            return null;
        }

        return fromDbItem(getItemResponse.item());
    }

    // Tested at integration level
    public void save(Transfer transfer) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("nhs_number", AttributeValue.builder().s(transfer.getNhsNumber()).build());
        item.put("conversation_id", AttributeValue.builder().s(transfer.getConversationId()).build());
        item.put("source_gp", AttributeValue.builder().s(transfer.getSourceGP()).build());
        item.put("nems_message_id", AttributeValue.builder().s(transfer.getNemsMessageId()).build());
        item.put("nems_event_last_updated", AttributeValue.builder().s(transfer.getNemsEventLastUpdated()).build());
        item.put("created_at", AttributeValue.builder().s(transfer.getCreatedAt()).build());
        item.put("last_updated_at", AttributeValue.builder().s(transfer.getLastUpdatedAt()).build());
        item.put("state", AttributeValue.builder().s(transfer.getState()).build());
        item.put("large_ehr_core_message_id", AttributeValue.builder().s(transfer.getLargeEhrCoreMessageId()).build());
        item.put("is_active", AttributeValue.builder().s((
                transfer.isActive() ? "true" : "false"))
                .build());

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(config.transferTrackerDbTableName())
                .item(item)
                .build());
    }

    public void update(String conversationId, String state, String lastUpdatedAt, boolean isActive) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("conversation_id", AttributeValue.builder().s(conversationId).build());

        Map<String, String> expressionAttributeName =
                new HashMap<>();
        expressionAttributeName.put("#state", "state");
        expressionAttributeName.put("#last_updated_at", "last_updated_at");

        Map<String, AttributeValue> expressionAttributeValues =
                new HashMap<>();
        expressionAttributeValues.put(":state", AttributeValue.builder().s(state).build());
        expressionAttributeValues.put(":last_updated_at", AttributeValue.builder().s(lastUpdatedAt).build());

        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(config.transferTrackerDbTableName())
                .key(key)
                .updateExpression(createUpdateExpression(isActive))
                .expressionAttributeNames(expressionAttributeName)
                .expressionAttributeValues(expressionAttributeValues)
                .build());
    }

    private String createUpdateExpression(boolean isActive) {
        var updateStateAndLastUpdatedAt = "set #state = :state, #last_updated_at = :last_updated_at";
        return (isActive)
                ? updateStateAndLastUpdatedAt
                : "remove is_active " + updateStateAndLastUpdatedAt;
    }

    public void updateLargeEhrCoreMessageId(String conversationId, String largeEhrCoreMessageId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("conversation_id", AttributeValue.builder().s(conversationId).build());

        Map<String, AttributeValueUpdate> updates = new HashMap<>();
        updates.put("large_ehr_core_message_id", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(largeEhrCoreMessageId).build())
                .build());

        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(config.transferTrackerDbTableName())
                .key(key)
                .attributeUpdates(updates)
                .build());
    }

    private Transfer fromDbItem(Map<String, AttributeValue> item) {

        var conversationId = item.get("conversation_id").s();
        var nhsNumber = item.get("nhs_number").s();
        var sourceGp = item.get("source_gp").s();
        var nemsMessageId = item.get("nems_message_id").s();
        var nemsEventLastUpdated = item.get("nems_event_last_updated").s();
        var createdAt = item.get("created_at").s();
        var lastUpdatedAt = item.get("last_updated_at").s();
        var state = item.get("state").s();
        var largeEhrCoreMessageId = item.get("large_ehr_core_message_id").s();
        var active = item.get("is_active");
        var isActive = active != null;
        return new Transfer(conversationId, nhsNumber, sourceGp, nemsMessageId, nemsEventLastUpdated, state, createdAt, lastUpdatedAt, largeEhrCoreMessageId, isActive);
    }

    public List<Transfer> getTimedOutRecords(String timeOutTimeStamp) {
        Map<String, String> expressionAttributeName =
                new HashMap<>();
        expressionAttributeName.put("#is_active", "is_active");
        expressionAttributeName.put("#created_at", "created_at");

        Map<String, AttributeValue> expressionAttributeValues =
                new HashMap<>();
        expressionAttributeValues.put(":is_active_val", AttributeValue.builder().s("true").build());
        expressionAttributeValues.put(":timeout_timestamp_val", AttributeValue.builder().s(timeOutTimeStamp).build());

        QueryRequest request = QueryRequest.builder().indexName("IsActiveSecondaryIndex")
                .tableName(config.transferTrackerDbTableName())
                .keyConditionExpression("#is_active = :is_active_val")
                .filterExpression("#created_at < :timeout_timestamp_val")
                .expressionAttributeNames(expressionAttributeName)
                .expressionAttributeValues(expressionAttributeValues)
                .build();
        log.info("Querying db for timed-out records {}", request);
        QueryResponse response = dynamoDbClient.query(request);

        if (response.hasItems()) {
            return getListOfDbEntries(response.items());
        }
        return new ArrayList<>();
    }

    private List<Transfer> getListOfDbEntries(List<Map<String, AttributeValue>> items) {
        List<Transfer> dbEntries = new ArrayList<>();
        for (Map<String, AttributeValue> item : items) {
            dbEntries.add(fromDbItem(item));
        }
        return dbEntries;
    }
}
