package uk.nhs.prm.repo.ehrtransferservice.database;

public enum TransferState {
    EHR_TRANSFER_STARTED(1),
    EHR_REQUEST_SENT_TO_GP2GP_MESSENGER(2),
    EHR_TRANSFER_TO_REPO_COMPLETE(3),
    EHR_TRANSFER_FAILED(4),
    EHR_TRANSFER_TIMEOUT(5);

    public final int code;

    TransferState(int code) {
        this.code = code;
    }
}
