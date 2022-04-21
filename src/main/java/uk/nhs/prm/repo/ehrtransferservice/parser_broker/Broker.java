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

    private final AttachmentMessagePublisher attachmentMessagePublisher;
    private final SmallEhrMessagePublisher smallEhrMessagePublisher;
    private final LargeEhrMessagePublisher largeEhrMessagePublisher;
    private final NegativeAcknowledgementMessagePublisher negativeAcknowledgementMessagePublisher;
    private final PositiveAcknowledgementMessagePublisher positiveAcknowledgementMessagePublisher;

    public void sendMessageToCorrespondingTopicPublisher(String interactionId, String message, UUID conversationId, boolean isLargeMessage, boolean isNegativeAck) {
        log.info("IN BROKER");
        switch (interactionId) {
            case ATTACHMENT_INTERACTION_ID:
                log.info("IDENTIFIED AS ATTACHMENT MESSAGE");
                attachmentMessagePublisher.sendMessage(message, conversationId);
                break;
            case EHR_EXTRACT_INTERACTION_ID:
                if (isLargeMessage) {
                    log.info("IDENTIFIED AS LARGE EHR EXTRACT");
                    largeEhrMessagePublisher.sendMessage(message, conversationId);
                    break;
                }
                log.info("IDENTIFIED AS SMALL EHR EXTRACT");
                smallEhrMessagePublisher.sendMessage(message, conversationId);
                break;
            case ACKNOWLEDGEMENT_INTERACTION_ID:
                if (isNegativeAck) {
                    log.info("IDENTIFIED AS NEGATIVE ACKNOWLEDGEMENT");
                    negativeAcknowledgementMessagePublisher.sendMessage(message, conversationId);
                    break;
                }
                log.info("IDENTIFIED AS POSITIVE ACKNOWLEDGEMENT");
                positiveAcknowledgementMessagePublisher.sendMessage(message, conversationId);
                break;
            default:
                log.warn("Unrecognised interaction ID - cannot identify message type");
                break;
        }
    }
}
