package uk.nhs.prm.repo.ehrtransferservice.database;

/**
 * An enumeration which represents all possible
 * attributes within the Transfer Tracker DynamoDB
 * table.
 */
public enum TransferTableAttribute {
    INBOUND_CONVERSATION_ID("InboundConversationId"),
    LAYER("Layer"),
    OUTBOUND_CONVERSATION_ID("OutboundConversationId"),
    NHS_NUMBER("NhsNumber"),
    SOURCE_GP("SourceGp"),
    DESTINATION_GP("DestinationGp"),
    TRANSFER_STATUS("TransferStatus"),
    FAILURE_CODE("FailureCode"),
    NEMS_MESSAGE_ID("NemsMessageId"),
    CREATED_AT("CreatedAt"),
    UPDATED_AT("UpdatedAt"),
    DELETED_AT("DeletedAt");

    public final String name;

    TransferTableAttribute(String name) {
        this.name = name;
    }
}
