package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
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
        item.put("nhs_number", AttributeValue.builder().n(transferTrackerDbEntry.getNhsNumber()).build());
        item.put("conversation_id", AttributeValue.builder().s(transferTrackerDbEntry.getConversationId()).build());
        item.put("source_gp", AttributeValue.builder().s(transferTrackerDbEntry.getSourceGP()).build());
        item.put("nems_message_id", AttributeValue.builder().s(transferTrackerDbEntry.getNemsMessageId()).build());
        item.put("date_time", AttributeValue.builder().s(transferTrackerDbEntry.getDateTime()).build());
        item.put("state", AttributeValue.builder().s(transferTrackerDbEntry.getState()).build());
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(config.transferTrackerDbTableName())
                .item(item)
                .build());
    }

    private TransferTrackerDbEntry fromDbItem(GetItemResponse itemResponse) {
        if (!itemResponse.hasItem()) {
            return null;
        }
        var conversationId = itemResponse.item().get("conversation_id").s();
        var nhsNumber = itemResponse.item().get("nhs_number").n();
        var sourceGp = itemResponse.item().get("source_gp").s();
        var nemsMessageId = itemResponse.item().get("nems_message_id").s();
        var dateTime = itemResponse.item().get("date_time").s();
        var state = itemResponse.item().get("state").s();
        return new TransferTrackerDbEntry(conversationId, nhsNumber, sourceGp, nemsMessageId, state, dateTime);
    }


}
