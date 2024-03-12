package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowledgement;
import uk.nhs.prm.repo.ehrtransferservice.services.AuditService;

import java.util.Optional;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class NegativeAcknowledgementHandler {
    private final TransferService transferService;
    private final AuditService auditService;

    public void handleMessage(Acknowledgement acknowledgement) {
        UUID conversationId = acknowledgement.getConversationId();
        Optional<UUID> nemsMessageId = transferService.getNemsMessageIdAsUuid(conversationId);

        logFailureDetail(acknowledgement);

        transferService.updateConversationTransferStatusWithFailure(
            conversationId,
            getFailureCodeForDb(acknowledgement)
        );

        auditService.publishAuditMessage(conversationId, INBOUND_FAILED, nemsMessageId);
    }

    private String getFailureCodeForDb(Acknowledgement acknowledgement) {
        if (acknowledgement.getFailureDetails().isEmpty()) return "UNKNOWN_ERROR";
        return acknowledgement.getFailureDetails().get(0).code();
    }

    private void logFailureDetail(Acknowledgement acknowledgement) {
        acknowledgement.getFailureDetails().forEach(detail -> log.info("""
            Negative acknowledgement details:
            
            acknowledgementTypeCode: {},
            detail.code: {},
            detail.displayName: {},
            detail.level: {},
            detail.codeSystem: {}
            """, acknowledgement.getTypeCode(), detail.code(), detail.displayName(), detail.level(), detail.codeSystem()));
    }
}