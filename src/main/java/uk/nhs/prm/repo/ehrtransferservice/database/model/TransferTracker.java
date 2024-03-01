package uk.nhs.prm.repo.ehrtransferservice.database.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class TransferTracker {
    private String inboundConversationId;

    @DynamoDbPartitionKey
    public String getInboundConversationId() {
        return this.inboundConversationId;
    }
}
