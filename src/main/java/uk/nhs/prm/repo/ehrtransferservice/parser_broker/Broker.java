package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.handlers.EhrRequestMessageHandler;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class Broker {
    private static final String LARGE_MESSAGE_FRAGMENT_INTERACTION_ID = "COPC_IN000001UK01";
    private static final String EHR_EXTRACT_INTERACTION_ID = "RCMR_IN030000UK06";
    private static final String ACKNOWLEDGEMENT_INTERACTION_ID = "MCCI_IN010000UK13";
    private static final String EHR_REQUEST_INTERACTION_ID = "RCMR_IN010000UK05";

    private final AttachmentMessagePublisher attachmentMessagePublisher;
    private final SmallEhrMessagePublisher smallEhrMessagePublisher;
    private final LargeEhrMessagePublisher largeEhrMessagePublisher;
    private final NegativeAcknowledgementMessagePublisher negativeAcknowledgementMessagePublisher;
    private final PositiveAcknowledgementMessagePublisher positiveAcknowledgementMessagePublisher;
    private final ParsingDlqPublisher parsingDlqPublisher;
    private final EhrRequestMessageHandler ehrRequestMessageHandler;

    public void sendMessageToCorrespondingTopicPublisher(ParsedMessage parsedMessage) {
        final var interactionId = parsedMessage.getInteractionId();
        final var message = parsedMessage.getRawMessage();
        final var conversationId = parsedMessage.getConversationId();
        switch (interactionId) {
            case EHR_EXTRACT_INTERACTION_ID:
                if (parsedMessage.isLargeMessage()) {
                    log.info("Message Type: LARGE MESSAGE CORE");
                    largeEhrMessagePublisher.sendMessage(message, conversationId);
                    break;
                }
                log.info("Message Type: SMALL EHR EXTRACT");
                smallEhrMessagePublisher.sendMessage(message, conversationId);
                break;
            case LARGE_MESSAGE_FRAGMENT_INTERACTION_ID:
                log.info("Message Type: LARGE MESSAGE FRAGMENT");
                attachmentMessagePublisher.sendMessage(message, conversationId);
                break;
            case ACKNOWLEDGEMENT_INTERACTION_ID:
                if (parsedMessage.isNegativeAcknowledgement()) {
                    log.info("Message Type: NEGATIVE ACKNOWLEDGEMENT");
                    negativeAcknowledgementMessagePublisher.sendMessage(message, conversationId);
                    break;
                }
                log.info("Message Type: POSITIVE ACKNOWLEDGEMENT");
                positiveAcknowledgementMessagePublisher.sendMessage(message, conversationId);
                break;
            case EHR_REQUEST_INTERACTION_ID:
                log.info("Message Type: EHR REQUEST");
                // TODO: Handler was added here temporarily for testing-
                //  leaving it so we can continue to test, but will need to refactor into topic/queue/handler pattern
                ehrRequestMessageHandler.handleMessage(parsedMessage);
                break;
            default:
                log.warn("Unknown Interaction ID: " + interactionId);
                parsingDlqPublisher.sendMessage(message);
                break;
        }
    }
}
