package uk.nhs.prm.repo.ehrtransferservice.database.enumeration;

public enum FailureReason {
    NEGATIVE_ACKNOWLEDGEMENT_RECEIVED("INBOUND:negative_acknowledgement_received");

    public final String reason;

    FailureReason(String reason) {
        this.reason = reason;
    }
}