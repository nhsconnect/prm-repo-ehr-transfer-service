package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.StoreMessageResult;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmallEhrMessageHandler implements MessageHandler<ParsedMessage> {

    private final EhrRepoService ehrRepoService;

    private final Gp2gpMessengerService gp2gpMessengerService;

    @Override
    public void handleMessage(ParsedMessage parsedMessage) throws Exception {
        final StoreMessageResult storeMessageResult = ehrRepoService.storeMessage(parsedMessage);
        log.info("The Small EHR with Inbound Message ID %s has been stored in the ehr-repo-service successfully"
            .formatted(parsedMessage.getConversationId()));

        if(storeMessageResult.isEhrComplete()) {
            gp2gpMessengerService.sendEhrCompletePositiveAcknowledgement(parsedMessage.getConversationId());
        }
    }
}