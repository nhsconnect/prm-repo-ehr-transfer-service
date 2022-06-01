package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.models.EhrCompleteEvent;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EhrCompleteMessagePublisherTest {

    @Mock
    private MessagePublisher messagePublisher;

    private final static String ehrCompleteTopicArn = "ehrCompleteTopicArn";
    private EhrCompleteMessagePublisher ehrCompleteMessagePublisher;
    private UUID conversationId;
    private UUID messageId;
    private EhrCompleteEvent ehrCompleteEvent;

    public EhrCompleteMessagePublisherTest() {
        conversationId = UUID.randomUUID();
        messageId = UUID.randomUUID();
    }

    @BeforeEach
    void setUp() {
        ehrCompleteMessagePublisher = new EhrCompleteMessagePublisher(messagePublisher, ehrCompleteTopicArn);
        ehrCompleteEvent = new EhrCompleteEvent(conversationId, messageId);
    }

    @Test
    void shouldPublishMessageToTheSmallEhrTopic() {
        ehrCompleteMessagePublisher.sendMessage(ehrCompleteEvent);
        verify(messagePublisher).sendJsonMessage(ehrCompleteTopicArn, ehrCompleteEvent, "conversationId", conversationId.toString());
    }

}