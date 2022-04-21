package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.*;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BrokerTest {
    @Mock
    AttachmentMessagePublisher attachmentMessagePublisher;
    @Mock
    SmallEhrMessagePublisher smallEhrMessagePublisher;
    @Mock
    LargeEhrMessagePublisher largeEhrMessagePublisher;
    @Mock
    NegativeAcknowledgementMessagePublisher negativeAcknowledgementMessagePublisher;
    @Mock
    PositiveAcknowledgementMessagePublisher positiveAcknowledgementMessagePublisher;

    @InjectMocks
    Broker broker;

    @Test
    public void shouldSendCopcMessageToAttachmentMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        broker.sendMessageToCorrespondingTopicPublisher("COPC_IN000001UK01", "copc-raw-message", conversationId, false, false);

        verify(attachmentMessagePublisher).sendMessage("copc-raw-message", conversationId);
    }

    @Test
    public void shouldSendSmallEhrMessageToSmallEhrMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        broker.sendMessageToCorrespondingTopicPublisher("RCMR_IN030000UK06", "ehr-raw-message", conversationId, false, false);

        verify(smallEhrMessagePublisher).sendMessage("ehr-raw-message", conversationId);
    }

    @Test
    public void shouldSendLargeEhrMessageToLargeEhrMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        broker.sendMessageToCorrespondingTopicPublisher("RCMR_IN030000UK06", "large-raw-message", conversationId, true, false);

        verify(largeEhrMessagePublisher).sendMessage("large-raw-message", conversationId);
    }

    @Test
    public void shouldSendNegativeAcknowledgementToNegativeAcknowledgementMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        broker.sendMessageToCorrespondingTopicPublisher("MCCI_IN010000UK13", "nack", conversationId, false, true);

        verify(negativeAcknowledgementMessagePublisher).sendMessage("nack", conversationId);
    }

    @Test
    public void shouldSendPositiveAcknowledgementToPositiveAcknowledgementMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        broker.sendMessageToCorrespondingTopicPublisher("MCCI_IN010000UK13", "positive-ack", conversationId, false, false);

        verify(positiveAcknowledgementMessagePublisher).sendMessage("positive-ack", conversationId);
    }
}
