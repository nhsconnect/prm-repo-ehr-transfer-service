package uk.nhs.prm.repo.ehrtransferservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.*;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowledgement;

@Component
@RequiredArgsConstructor
@Slf4j
public class Broker {
    private static final String LARGE_MESSAGE_FRAGMENT_INTERACTION_ID = "COPC_IN000001UK01";
    private static final String EHR_EXTRACT_INTERACTION_ID = "RCMR_IN030000UK06";
    private static final String ACKNOWLEDGEMENT_INTERACTION_ID = "MCCI_IN010000UK13";

    private final AttachmentMessagePublisher attachmentMessagePublisher;
    private final SmallEhrMessagePublisher smallEhrMessagePublisher;
    private final LargeEhrMessagePublisher largeEhrMessagePublisher;
    private final NegativeAcknowledgementMessagePublisher negativeAcknowledgementMessagePublisher;
    private final PositiveAcknowledgementMessagePublisher positiveAcknowledgementMessagePublisher;
    private final ParsingDlqPublisher parsingDlqPublisher;
    private final EhrInUnhandledMessagePublisher ehrInUnhandledMessagePublisher;

    private final TransferTrackerService transferTrackerService;

    private void sendMessageToCorrespondingTopicPublisher(ParsedMessage parsedMessage) {
        final var interactionId = parsedMessage.getInteractionId();
        final var message = parsedMessage.getMessageBody();
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
                var acknowledgement = (Acknowledgement) parsedMessage;
                if (acknowledgement.isNegativeAcknowledgement()) {
                    log.info("Message Type: NEGATIVE ACKNOWLEDGEMENT");
                    negativeAcknowledgementMessagePublisher.sendMessage(message, conversationId);
                    break;
                }
                log.info("Message Type: POSITIVE ACKNOWLEDGEMENT");
                positiveAcknowledgementMessagePublisher.sendMessage(message, conversationId);
                break;
            default:
                log.warn("Unknown Interaction ID: " + interactionId);
                parsingDlqPublisher.sendMessage(message);
                break;
        }
    }

    public void sendMessageToEhrInOrEhrOut(ParsedMessage parsedMessage) {
        boolean conversationIdPresent = transferTrackerService.isConversationIdPresent(parsedMessage.getConversationId().toString());

        if (conversationIdPresent) {
            log.info("Found conversation id in db - received EHR IN message");
            sendMessageToCorrespondingTopicPublisher(parsedMessage);
        } else {
            log.info("Did not find conversation id in db - sending to EHR IN Unhandled topic");
            ehrInUnhandledMessagePublisher.sendMessage(parsedMessage.getMessageBody(), parsedMessage.getConversationId());
        }
    }
}
