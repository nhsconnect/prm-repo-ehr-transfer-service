package uk.nhs.prm.repo.ehrtransferservice.timeout;


import org.springframework.scheduling.annotation.Scheduled;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerDb;

public class EhrRequestTimeoutHandler {
    TransferTrackerDb transferTrackerDb;

    @Scheduled()
    public void handle(){
        getConversationIdsForTimedOutRecords();
        updateAllTimeOutRecordsInDb();
        sendMessageToTransferCompleteQueue();

    }

    private void sendMessageToTransferCompleteQueue() {
    }

    private void updateAllTimeOutRecordsInDb() {
    }

    private String getConversationIdsForTimedOutRecords() {
        String timeout = null;
        transferTrackerDb.getTimedOutRecords(timeout);
        return null;
    }
}
