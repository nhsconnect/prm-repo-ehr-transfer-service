package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EhrCompleteMessagePublisherTest {
    @Mock
    private MessagePublisher messagePublisher;

    private final static String topicArn = "topicArn";

    private EhrCompleteMessagePublisher ehrCompleteMessagePublisher;

    @BeforeEach
    void setUp() {
        ehrCompleteMessagePublisher = new EhrCompleteMessagePublisher(messagePublisher, topicArn);
    }

    @Test
    void shouldPublishMessageToTheSmallEhrTopic() {
        var conversationId = UUID.randomUUID();
        ehrCompleteMessagePublisher.sendMessage("message", conversationId);
        verify(messagePublisher).sendMessage(topicArn, "message", "conversationId", conversationId.toString());
    }

}