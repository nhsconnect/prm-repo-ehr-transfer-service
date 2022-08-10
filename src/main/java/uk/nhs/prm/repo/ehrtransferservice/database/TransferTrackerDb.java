package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import uk.nhs.prm.repo.ehrtransferservice.config.AppConfig;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TransferTrackerDb {
    private final DynamoDbClient dynamoDbClient;
    private final AppConfig config;

    public TransferTrackerDbEntry getByConversationId(String conversationId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("conversation_id", AttributeValue.builder().s(conversationId).build());
        var getItemResponse = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(config.transferTrackerDbTableName())
                .key(key)
                .build());

        return fromDbItem(getItemResponse);
    }

    // Tested at integration level
    public void save(TransferTrackerDbEntry transferTrackerDbEntry) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("nhs_number", AttributeValue.builder().s(transferTrackerDbEntry.getNhsNumber()).build());
        item.put("conversation_id", AttributeValue.builder().s(transferTrackerDbEntry.getConversationId()).build());
        item.put("source_gp", AttributeValue.builder().s(transferTrackerDbEntry.getSourceGP()).build());
        item.put("nems_message_id", AttributeValue.builder().s(transferTrackerDbEntry.getNemsMessageId()).build());
        item.put("nems_event_last_updated", AttributeValue.builder().s(transferTrackerDbEntry.getNemsEventLastUpdated()).build());
        item.put("created_at", AttributeValue.builder().s(transferTrackerDbEntry.getCreatedAt()).build());
        item.put("last_updated_at", AttributeValue.builder().s(transferTrackerDbEntry.getLastUpdatedAt()).build());
        item.put("state", AttributeValue.builder().s(transferTrackerDbEntry.getState()).build());
        item.put("large_ehr_core_message_id", AttributeValue.builder().s(transferTrackerDbEntry.getLargeEhrCoreMessageId()).build());
        item.put("is_active", AttributeValue.builder().s(transferTrackerDbEntry.getIsActive().toString()).build());
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(config.transferTrackerDbTableName())
                .item(item)
                .build());
    }

    public void update(String conversationId, String state, String lastUpdatedAt) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("conversation_id", AttributeValue.builder().s(conversationId).build());

        Map<String, AttributeValueUpdate> updates = new HashMap<>();
        updates.put("state", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(state).build())
                .build());
        updates.put("last_updated_at", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(lastUpdatedAt).build())
                .build());

        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(config.transferTrackerDbTableName())
                .key(key)
                .attributeUpdates(updates)
                .build());
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

    private TransferTrackerDbEntry fromDbItem(GetItemResponse itemResponse) {
        if (!itemResponse.hasItem()) {
            return null;
        }
        var conversationId = itemResponse.item().get("conversation_id").s();
        var nhsNumber = itemResponse.item().get("nhs_number").s();
        var sourceGp = itemResponse.item().get("source_gp").s();
        var nemsMessageId = itemResponse.item().get("nems_message_id").s();
        var nemsEventLastUpdated = itemResponse.item().get("nems_event_last_updated").s();
        var createdAt = itemResponse.item().get("created_at").s();
        var lastUpdatedAt = itemResponse.item().get("last_updated_at").s();
        var state = itemResponse.item().get("state").s();
        var largeEhrCoreMessageId = itemResponse.item().get("large_ehr_core_message_id").s();
        var active = itemResponse.item().get("is_active");
        var isActive = (active == null) ? false : true;
        return new TransferTrackerDbEntry(conversationId, nhsNumber, sourceGp, nemsMessageId, nemsEventLastUpdated, state, createdAt, lastUpdatedAt, largeEhrCoreMessageId, isActive);
    }
}
