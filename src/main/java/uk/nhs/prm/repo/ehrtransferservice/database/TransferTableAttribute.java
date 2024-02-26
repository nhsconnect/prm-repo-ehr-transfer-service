package uk.nhs.prm.repo.ehrtransferservice.database;

/**
 * An enumeration which represents all possible
 * attributes within the Transfer Tracker DynamoDB
 * table.
 */
public enum TransferTableAttribute {
    INBOUND_CONVERSATION_ID("InboundConversationId"),
    OUTBOUND_CONVERSATION_ID("OutboundConversationId"),
    NHS_NUMBER("NhsNumber"),
    SOURCE_GP("SourceGp"),
    DESTINATION_GP("DestinationGp"),
    STATE("State"),
    MESH_MESSAGE_ID("MeshMessageId"),
    NEMS_MESSAGE_ID("NemsMessageId"),
    CREATED_AT("CreatedAt"),
    UPDATED_AT("UpdatedAt"),
    DELETED_AT("DeletedAt");

    public final String name;

    TransferTableAttribute(String name) {
        this.name = name;
    }
}
