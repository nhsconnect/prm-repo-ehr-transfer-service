package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.json_models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.json_models.TransferCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EhrCompleteHandler {
    private final Gp2gpMessengerService gp2gpMessengerService;
    private final TransferTrackerService transferTrackerService;
    private static final String TRANSFER_TO_REPO_COMPLETE = "ACTION:EHR_TRANSFER_TO_REPO_COMPLETE";

    public void handleMessage(EhrCompleteEvent ehrCompleteEvent) throws Exception {
        var conversationId = ehrCompleteEvent.getConversationId().toString();
        var ehrTransferData = transferTrackerService.getEhrTransferData(conversationId);

        gp2gpMessengerService.sendEhrCompletePositiveAcknowledgement(ehrCompleteEvent, ehrTransferData);
        transferTrackerService.updateStateOfEhrTransfer(conversationId, TRANSFER_TO_REPO_COMPLETE);
        createTransferCompleteEvent(ehrTransferData);
        // Put EHR complete message on the transfer complete topic
    }

    private TransferCompleteEvent createTransferCompleteEvent(TransferTrackerDbEntry ehrTransferData) {
        // Update last updated value from null to real value, once it is in db (story #2656)
        return new TransferCompleteEvent(
                null, ehrTransferData.getSourceGP(),
                "SUSPENSION", ehrTransferData.getNemsMessageId(),
                ehrTransferData.getNhsNumber()
        );
    }
}
