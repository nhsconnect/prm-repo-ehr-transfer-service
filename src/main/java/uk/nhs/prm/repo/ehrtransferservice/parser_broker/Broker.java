package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.AttachmentMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.SmallEhrMessagePublisher;

@Component
@RequiredArgsConstructor
public class Broker {
    private final AttachmentMessagePublisher attachmentMessagePublisher;
    private final SmallEhrMessagePublisher smallEhrMessagePublisher;
    private static final String ATTACHMENT_INTERACTION_ID = "COPC_IN000001UK01";
    private static final String EHR_EXTRACT_INTERACTION_ID = "RCMR_IN030000UK06";

    public void sendMessageToCorrespondingTopicPublisher(String interactionId, ParsedMessage parsedMessage) {
        switch (interactionId) {
            case ATTACHMENT_INTERACTION_ID:
                attachmentMessagePublisher.sendMessage(parsedMessage.getRawMessage(), parsedMessage.getConversationId());
                break;
            case EHR_EXTRACT_INTERACTION_ID:
                smallEhrMessagePublisher.sendMessage(parsedMessage.getRawMessage(), parsedMessage.getConversationId());
                break;
            default:
                break;
        }
    }
}
