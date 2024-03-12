package uk.nhs.prm.repo.ehrtransferservice.database.enumeration;

public enum ConversationTransferStatus {
    INBOUND_STARTED,
    INBOUND_REQUEST_SENT,
    INBOUND_CONTINUE_REQUEST_SENT,
    INBOUND_COMPLETE,
    INBOUND_FAILED,
    INBOUND_TIMEOUT,
    OUTBOUND_STARTED,
    OUTBOUND_COMPLETE,
    OUTBOUND_FAILED,
    OUTBOUND_TIMEOUT
}