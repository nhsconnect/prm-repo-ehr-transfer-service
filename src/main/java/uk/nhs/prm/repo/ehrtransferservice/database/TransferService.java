package uk.nhs.prm.repo.ehrtransferservice.database;

import com.amazonaws.SdkClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferRecordNotPresentException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferUnableToPersistException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferUnableToUpdateException;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.TransferTableAttribute.STATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;

    public void createConversation(RepoIncomingEvent event) {
        final UUID inboundConversationId = UUID.fromString(event.getConversationId());

        try {
            transferRepository.createConversation(event);
            log.info("Conversation DynamoDB entry created for Inbound Conversation ID {}.", event.getConversationId());
        } catch (SdkClientException exception) {
            throw new TransferUnableToPersistException(inboundConversationId, exception);
        }
    }

    public String getConversationStatus(UUID inboundConversationId) {
        GetItemResponse response = transferRepository.findConversationByInboundConversationId(inboundConversationId);

        if (response.hasItem()) {
            return response.item().get(STATE.name).s();
        } else {
            throw new TransferRecordNotPresentException(inboundConversationId);
        }
    }

    public boolean isInboundConversationIdPresent(UUID inboundConversationId) {
        return transferRepository
            .findConversationByInboundConversationId(inboundConversationId)
            .hasItem();
    }

    public void updateConversationStatus(UUID inboundConversationId, TransferState state) {
        try {
            transferRepository.updateConversationStatus(inboundConversationId, state);
        } catch (SdkClientException exception) {
            throw new TransferUnableToUpdateException(inboundConversationId, exception);
        }
    }
}