package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.*;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class Broker {
    private static final String ATTACHMENT_INTERACTION_ID = "COPC_IN000001UK01";
    private static final String EHR_EXTRACT_INTERACTION_ID = "RCMR_IN030000UK06";
    private static final String ACKNOWLEDGEMENT_INTERACTION_ID = "MCCI_IN010000UK13";
    private static final String EHR_REQUEST_INTERACTION_ID = "RCMR_IN010000UK05";

    private final AttachmentMessagePublisher attachmentMessagePublisher;
    private final SmallEhrMessagePublisher smallEhrMessagePublisher;
    private final LargeEhrMessagePublisher largeEhrMessagePublisher;
    private final NegativeAcknowledgementMessagePublisher negativeAcknowledgementMessagePublisher;
    private final PositiveAcknowledgementMessagePublisher positiveAcknowledgementMessagePublisher;

    public void sendMessageToCorrespondingTopicPublisher(String interactionId, String message, UUID conversationId, boolean isLargeMessage, boolean isNegativeAck) {
        switch (interactionId) {
            case ATTACHMENT_INTERACTION_ID:
                log.info("Message Type: ATTACHMENT");
                attachmentMessagePublisher.sendMessage(message, conversationId);
                break;
            case EHR_EXTRACT_INTERACTION_ID:
                if (isLargeMessage) {
                    log.info("Message Type: LARGE EHR EXTRACT");
                    largeEhrMessagePublisher.sendMessage(message, conversationId);
                    break;
                }
                log.info("Message Type: SMALL EHR EXTRACT");
                smallEhrMessagePublisher.sendMessage(message, conversationId);
                break;
            case ACKNOWLEDGEMENT_INTERACTION_ID:
                if (isNegativeAck) {
                    log.info("Message Type: NEGATIVE ACKNOWLEDGEMENT");
                    negativeAcknowledgementMessagePublisher.sendMessage(message, conversationId);
                    break;
                }
                log.info("Message Type: POSITIVE ACKNOWLEDGEMENT");
                positiveAcknowledgementMessagePublisher.sendMessage(message, conversationId);
                break;
            case EHR_REQUEST_INTERACTION_ID:
                log.info("Message Type: EHR REQUEST - Not currently handled until Repo OUT");
                break;
            default:
                log.warn("Unknown Interaction ID: " + interactionId);
                break;
        }
    }
}
