package uk.nhs.prm.repo.ehrtransferservice.database;

public enum TransferState {
    EHR_TRANSFER_STARTED(1);

    public final int code;

    TransferState(int code) {
        this.code = code;
    }
}
