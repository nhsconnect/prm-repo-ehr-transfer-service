package uk.nhs.prm.repo.ehrtransferservice.message_publishers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.models.TransferCompleteEvent;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransferCompleteMessagePublisherTest {

    @Mock
    private MessagePublisher messagePublisher;

    private final static String transferCompleteTopicArn = "transferCompleteTopicArn";
    private TransferCompleteMessagePublisher transferCompleteMessagePublisher;
    private UUID conversationId = UUID.randomUUID();
    private TransferCompleteEvent transferCompleteEvent;

    @BeforeEach
    void setUp() {
        transferCompleteMessagePublisher = new TransferCompleteMessagePublisher(messagePublisher, transferCompleteTopicArn);
        transferCompleteEvent = new TransferCompleteEvent(null, "some-ods-code", "SUSPENSION", "some-nems-message-id", "some-nhs-number");
    }

    @Test
    void shouldPublishMessageToTheSmallEhrTopic() {
        transferCompleteMessagePublisher.sendMessage(transferCompleteEvent, conversationId);
        verify(messagePublisher).sendJsonMessage(transferCompleteTopicArn, transferCompleteEvent, "conversationId", conversationId.toString());
    }

}