package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.*;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowledgement;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    @Mock
    ParsingDlqPublisher parsingDlqPublisher;

    @InjectMocks
    Broker broker;

    private ParsedMessage getMockParsedMessage(String interactionId, String rawMessage, UUID conversationId) {
        return getMockParsedMessage(interactionId, rawMessage, conversationId, ParsedMessage.class);
    }

    private <T extends ParsedMessage> T getMockParsedMessage(String interactionId, String rawMessage, UUID conversationId, Class<T> messageClass) {
        var parsedMessage = mock(messageClass);
        when(parsedMessage.getInteractionId()).thenReturn(interactionId);
        when(parsedMessage.getRawMessage()).thenReturn(rawMessage);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        return parsedMessage;
    }

    @Test
    public void shouldSendCopcMessageToAttachmentMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        var rawMessage = "copc-raw-message";
        var parsedMessage = getMockParsedMessage("COPC_IN000001UK01", rawMessage, conversationId);

        broker.sendMessageToCorrespondingTopicPublisher(parsedMessage);

        verify(attachmentMessagePublisher).sendMessage(rawMessage, conversationId);
    }

    @Test
    public void shouldSendSmallEhrMessageToSmallEhrMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        var rawMessage = "ehr-raw-message";
        var parsedMessage = getMockParsedMessage("RCMR_IN030000UK06", rawMessage, conversationId);

        broker.sendMessageToCorrespondingTopicPublisher(parsedMessage);

        verify(smallEhrMessagePublisher).sendMessage(rawMessage, conversationId);
    }

    @Test
    public void shouldSendLargeEhrMessageToLargeEhrMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        var rawMessage = "copc-raw-message";
        var parsedMessage = getMockParsedMessage("RCMR_IN030000UK06", rawMessage, conversationId);
        when(parsedMessage.isLargeMessage()).thenReturn(true);

        broker.sendMessageToCorrespondingTopicPublisher(parsedMessage);

        verify(largeEhrMessagePublisher).sendMessage(rawMessage, conversationId);
    }

    @Test
    public void shouldSendNegativeAcknowledgementToNegativeAcknowledgementMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        var rawMessage = "nack";
        var acknowledgement = getMockParsedMessage("MCCI_IN010000UK13", rawMessage, conversationId, Acknowledgement.class);
        when(acknowledgement.isNegativeAcknowledgement()).thenReturn(true);

        broker.sendMessageToCorrespondingTopicPublisher(acknowledgement);

        verify(negativeAcknowledgementMessagePublisher).sendMessage("nack", conversationId);
    }

    @Test
    public void shouldSendPositiveAcknowledgementToPositiveAcknowledgementMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        var rawMessage = "positive-ack";
        var acknowledgement = getMockParsedMessage("MCCI_IN010000UK13", rawMessage, conversationId, Acknowledgement.class);

        broker.sendMessageToCorrespondingTopicPublisher(acknowledgement);

        verify(positiveAcknowledgementMessagePublisher).sendMessage("positive-ack", conversationId);
    }

    @Test
    public void shouldSendUnreckognizedMessagesToDlq()  {
        var conversationId = UUID.randomUUID();
        var parsedMessage = getMockParsedMessage("something-unreckognizable", "some-ack", conversationId);

        broker.sendMessageToCorrespondingTopicPublisher(parsedMessage);

        verify(parsingDlqPublisher).sendMessage(any());
    }
}
