package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmallEhrMessagePublisherTest {
    @Mock
    private MessagePublisher messagePublisher;

    private final static String topicArn = "topicArn";

    private SmallEhrMessagePublisher smallEhrMessagePublisher;

    @BeforeEach
    void setUp() {
        smallEhrMessagePublisher = new SmallEhrMessagePublisher(messagePublisher, topicArn);
    }

    @Test
    void shouldPublishMessageToTheSmallEhrTopic() {
        var conversationId = UUID.randomUUID();
        smallEhrMessagePublisher.sendMessage("message", conversationId);
        verify(messagePublisher).sendMessage(topicArn, "message", "conversationId", conversationId.toString());
    }
}