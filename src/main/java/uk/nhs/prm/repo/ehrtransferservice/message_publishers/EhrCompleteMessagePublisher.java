package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.json_models.EhrCompleteEvent;

import java.util.UUID;

@Component
public class EhrCompleteMessagePublisher {
    private final String ehrCompleteTopicArn;
    private final MessagePublisher messagePublisher;

    public EhrCompleteMessagePublisher(MessagePublisher messagePublisher, @Value("${aws.ehrCompleteTopicArn}") String ehrCompleteTopicArn) {
        this.messagePublisher = messagePublisher;
        this.ehrCompleteTopicArn = ehrCompleteTopicArn;
    }

    public void sendMessage(EhrCompleteEvent ehrCompleteEvent) {
        messagePublisher.sendJsonMessage(this.ehrCompleteTopicArn, ehrCompleteEvent, "conversationId", ehrCompleteEvent.getConversationId().toString());
    }
}

