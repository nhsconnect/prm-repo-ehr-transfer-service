package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_COMPLETE;

@Slf4j
@Service
@RequiredArgsConstructor
public class EhrCompleteHandler {
    private final Gp2gpMessengerService gp2gpMessengerService;
    private final TransferService transferService;

    public void handleMessage(EhrCompleteEvent ehrCompleteEvent) throws Exception {
        final UUID inboundConversationId = ehrCompleteEvent.getConversationId();
        final ConversationRecord conversation = transferService
            .getConversationByInboundConversationId(inboundConversationId);
        final UUID ehrCoreMessageId = transferService
            .getEhrCoreInboundMessageIdForInboundConversationId(inboundConversationId);

        gp2gpMessengerService.sendEhrCompletePositiveAcknowledgement(
            conversation.nhsNumber(),
            conversation.sourceGp(),
            inboundConversationId,
            ehrCoreMessageId
        );

        transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_COMPLETE);
    }
}