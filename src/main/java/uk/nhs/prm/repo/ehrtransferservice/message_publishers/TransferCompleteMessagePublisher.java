package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.models.TransferCompleteEvent;

import java.util.Map;
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
        var attributes = Map.of("conversationId", conversationId.toString(), "nemsMessageId", transferCompleteEvent.getNemsMessageId());
        messagePublisher.sendJsonMessage(this.transferCompleteTopicArn, transferCompleteEvent, attributes);
    }
}