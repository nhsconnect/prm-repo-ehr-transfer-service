package uk.nhs.prm.repo.ehrtransferservice.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.CoreTransferStatus.OUTBOUND_SENT;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.TransferTableAttribute.*;
import static uk.nhs.prm.repo.ehrtransferservice.utility.DateUtility.getIsoTimestamp;

@Component
public final class TransferTrackerDataGenerator {
    private static final Logger LOGGER = LogManager.getLogger(TransferTrackerDataGenerator.class);
    private final DynamoDbClient dynamoDbClient;
    private final String transferTrackerDbTableName;
    private static final String CORE_LAYER = "CORE#%s";

    @Autowired
    public TransferTrackerDataGenerator(DynamoDbClient dynamoDbClient,
                                        @Value("${aws.transferTrackerDbTableName}") String transferTrackerDbTableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.transferTrackerDbTableName = transferTrackerDbTableName;
    }

    public void createCore(UUID inboundConversationId, UUID ehrCoreMessageId) {
        final Map<String, AttributeValue> tableItem = new HashMap<>();
        final String timestamp = getIsoTimestamp();

        tableItem.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(inboundConversationId.toString())
            .build());

        tableItem.put(LAYER.name, AttributeValue.builder()
            .s(CORE_LAYER.formatted(ehrCoreMessageId.toString())).build());

        tableItem.put(INBOUND_MESSAGE_ID.name, AttributeValue.builder()
            .s(ehrCoreMessageId.toString()).build());

        tableItem.put(TRANSFER_STATUS.name, AttributeValue.builder()
            .s(OUTBOUND_SENT.name())
            .build());

        tableItem.put(CREATED_AT.name, AttributeValue.builder()
            .s(timestamp)
            .build());

        tableItem.put(UPDATED_AT.name, AttributeValue.builder()
            .s(timestamp)
            .build());

        final PutItemRequest itemRequest = PutItemRequest.builder()
            .tableName(transferTrackerDbTableName)
            .item(tableItem)
            .build();

        dynamoDbClient.putItem(itemRequest);
        LOGGER.info("The CORE layer has been crated for Inbound Conversation ID: {}",
            inboundConversationId.toString());
    }
}
