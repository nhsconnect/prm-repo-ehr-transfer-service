package uk.nhs.prm.repo.ehrtransferservice.database.enumeration;

public enum ConversationTransferStatus {
    INBOUND_STARTED(false),
    INBOUND_REQUEST_SENT(false),
    INBOUND_CONTINUE_REQUEST_SENT(false),
    INBOUND_COMPLETE(true),
    INBOUND_FAILED(true),
    INBOUND_TIMEOUT(true);

    public final boolean isTerminating;

    ConversationTransferStatus(boolean isTerminating) {
        this.isTerminating = isTerminating;
    }
}