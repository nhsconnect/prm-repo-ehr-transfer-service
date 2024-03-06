package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LargeEhrCoreMessageHandler implements MessageHandler<ParsedMessage> {

    private final EhrRepoService ehrRepoService;

    private final Gp2gpMessengerService gp2gpMessengerService;

    private final TransferService transferService;

    @Override
    public void handleMessage(ParsedMessage largeEhrCoreMessage) throws Exception {
        UUID conversationId = largeEhrCoreMessage.getConversationId();

        ehrRepoService.storeMessage(largeEhrCoreMessage);

        log.info("Successfully stored large-ehr message in the ehr-repo");

        ConversationRecord conversation = transferService.getConversation(conversationId);

        gp2gpMessengerService.sendContinueMessage(largeEhrCoreMessage, conversation.sourceGp());

        transferService.updateConversationStatus(
                conversationId,
                String.valueOf(conversation.nemsMessageId()
                        .orElse(null)),
                TransferStatus.CONTINUE_REQUEST_SENT_TO_GP2GP_MESSENGER
        );
    }
}