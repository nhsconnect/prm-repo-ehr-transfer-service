package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AttachmentMessagePublisher {
    private final String attachmentsTopicArn;
    private final MessagePublisher messagePublisher;

    public AttachmentMessagePublisher(MessagePublisher messagePublisher, @Value("${aws.attachmentsTopicArn}") String attachmentsTopicArn) {
        this.messagePublisher = messagePublisher;
        this.attachmentsTopicArn = attachmentsTopicArn;
    }

    public void sendMessage(String message, UUID conversationId) {
        messagePublisher.sendMessage(this.attachmentsTopicArn, message, "conversationId", conversationId.toString());
    }
}

