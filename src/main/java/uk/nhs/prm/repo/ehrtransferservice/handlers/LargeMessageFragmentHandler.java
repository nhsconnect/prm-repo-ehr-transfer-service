package uk.nhs.prm.repo.ehrtransferservice.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeEhrFragmentMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.StoreMessageResult;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LargeMessageFragmentHandler implements MessageHandler<ParsedMessage> {
    private final Gp2gpMessengerService gp2gpMessengerService;
    private final EhrRepoService ehrRepoService;

    @Override
    public void handleMessage(ParsedMessage fragmentMessage) throws Exception {
        StoreMessageResult storeMessageResult = storeFragmentMessage(fragmentMessage);

        if (storeMessageResult.isEhrComplete()) {
            log.info("Successfully stored all fragments for Inbound Conversation ID {}",
                    getIdAsUpperCasedStringIfNotNull(fragmentMessage.getConversationId()));
            gp2gpMessengerService.sendEhrCompletePositiveAcknowledgement(fragmentMessage.getConversationId());
        }
    }

    private StoreMessageResult storeFragmentMessage(ParsedMessage fragmentMessage) throws Exception {
        LargeEhrFragmentMessage largeEhrFragment = new LargeEhrFragmentMessage(fragmentMessage);
        StoreMessageResult storeMessageResult = ehrRepoService.storeMessage(largeEhrFragment);

        log.info("Successfully stored fragment with Inbound Message ID {} for Inbound Conversation ID {}",
                getIdAsUpperCasedStringIfNotNull(fragmentMessage.getMessageId()),
                getIdAsUpperCasedStringIfNotNull(fragmentMessage.getConversationId()));

        return storeMessageResult;
    }

    /**
     * ParsedMessage getters can return null, wrapping as optional to avoid NullPointerException
     * @param id the UUID to uppercase
     * @return UUID as uppercased string
     */
    private String getIdAsUpperCasedStringIfNotNull(UUID id) {
        return id != null
                ? id.toString().toUpperCase()
                : null;
    }
}