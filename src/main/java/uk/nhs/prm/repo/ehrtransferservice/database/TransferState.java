package uk.nhs.prm.repo.ehrtransferservice.database;

public enum TransferState {
    EHR_REQUEST_SENT(1),
    LARGE_EHR_CONTINUE_REQUEST_SENT(2),
    TRANSFER_TO_REPO_STARTED(3),
    EHR_TRANSFER_TO_REPO_COMPLETE(4),
    EHR_TRANSFER_FAILED(5),
    EHR_TRANSFER_TIMEOUT(6);

    public final int code;

    TransferState(int code) {
        this.code = code;
    }
}
