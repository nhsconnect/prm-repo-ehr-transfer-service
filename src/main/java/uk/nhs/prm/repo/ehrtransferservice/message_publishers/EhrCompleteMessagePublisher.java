package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EhrCompleteMessagePublisher {
    private final String ehrCompleteTopicArn;
    private final MessagePublisher messagePublisher;

    public EhrCompleteMessagePublisher(MessagePublisher messagePublisher, @Value("${aws.ehrCompleteTopicArn}") String ehrCompleteTopicArn) {
        this.messagePublisher = messagePublisher;
        this.ehrCompleteTopicArn = ehrCompleteTopicArn;
    }

    public void sendMessage(String message, UUID conversationId) {
        messagePublisher.sendMessage(this.ehrCompleteTopicArn, message, "conversationId", conversationId.toString());
    }
}

