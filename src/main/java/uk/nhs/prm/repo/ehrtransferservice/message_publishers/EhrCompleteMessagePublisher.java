package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.ehrtransferservice.models.EhrCompleteEvent;

import java.util.Map;

@Component
@Deprecated(forRemoval = true, since = "07/03/2024")
public class EhrCompleteMessagePublisher {
    private final String ehrCompleteTopicArn;
    private final MessagePublisher messagePublisher;

    public EhrCompleteMessagePublisher(MessagePublisher messagePublisher, @Value("${aws.ehrCompleteTopicArn}") String ehrCompleteTopicArn) {
        this.messagePublisher = messagePublisher;
        this.ehrCompleteTopicArn = ehrCompleteTopicArn;
    }

    public void sendMessage(EhrCompleteEvent ehrCompleteEvent) {
        messagePublisher.sendJsonMessage(this.ehrCompleteTopicArn, ehrCompleteEvent,Map.of("conversationId", ehrCompleteEvent.getConversationId().toString()));
    }
}

