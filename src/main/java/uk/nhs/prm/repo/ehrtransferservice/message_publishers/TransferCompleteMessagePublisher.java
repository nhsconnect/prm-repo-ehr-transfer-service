package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.json_models.TransferCompleteEvent;

import java.util.UUID;

@Component
public class TransferCompleteMessagePublisher {
    private final String transferCompleteTopicArn;
    private final MessagePublisher messagePublisher;

    public TransferCompleteMessagePublisher(MessagePublisher messagePublisher, @Value("${aws.transferCompleteTopicArn}") String transferCompleteTopicArn) {
        this.messagePublisher = messagePublisher;
        this.transferCompleteTopicArn = transferCompleteTopicArn;
    }

    public void sendMessage(TransferCompleteEvent transferCompleteEvent, UUID conversationId) {
        messagePublisher.sendJsonMessage(this.transferCompleteTopicArn, transferCompleteEvent, "conversationId", conversationId.toString());
    }
}
