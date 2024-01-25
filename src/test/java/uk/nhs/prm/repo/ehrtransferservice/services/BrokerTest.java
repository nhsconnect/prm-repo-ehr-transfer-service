package uk.nhs.prm.repo.ehrtransferservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferStore;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.*;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowledgement;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BrokerTest {
    @Mock
    FragmentMessagePublisher fragmentMessagePublisher;
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
    @Mock
    EhrInUnhandledMessagePublisher ehrInUnhandledMessagePublisher;
    @Mock
    TransferStore transferStore;

    @InjectMocks
    Broker broker;

    private ParsedMessage getMockParsedMessage(String interactionId, String messageBody, UUID conversationId) {
        return getMockParsedMessage(interactionId, messageBody, conversationId, ParsedMessage.class);
    }

    private <T extends ParsedMessage> T getMockParsedMessage(String interactionId, String messageBody, UUID conversationId, Class<T> messageClass) {
        var parsedMessage = mock(messageClass);
        when(parsedMessage.getInteractionId()).thenReturn(interactionId);
        when(parsedMessage.getMessageBody()).thenReturn(messageBody);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        return parsedMessage;
    }

    private ParsedMessage getMockParsedMessageWithoutInteractionId(String messageBody, UUID conversationId) {
        return getMockParsedMessageWithoutInteractionId(messageBody, conversationId, ParsedMessage.class);
    }

    private <T extends ParsedMessage> T getMockParsedMessageWithoutInteractionId(String messageBody, UUID conversationId, Class<T> messageClass) {
        var parsedMessage = mock(messageClass);
        when(parsedMessage.getMessageBody()).thenReturn(messageBody);
        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        return parsedMessage;
    }

    @Test
    public void shouldSendCopcMessageToFragmentMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        var messageBody = "copc-message";
        var parsedMessage = getMockParsedMessage("COPC_IN000001UK01", messageBody, conversationId);

        when(transferStore.isConversationIdPresent(conversationId.toString())).thenReturn(true);

        broker.sendMessageToEhrInOrUnhandled(parsedMessage);

        verify(fragmentMessagePublisher).sendMessage(messageBody, conversationId);
    }

    @Test
    public void shouldSendSmallEhrMessageToSmallEhrMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        var messageBody = "ehr-message";
        var parsedMessage = getMockParsedMessage("RCMR_IN030000UK06", messageBody, conversationId);

        when(transferStore.isConversationIdPresent(conversationId.toString())).thenReturn(true);

        broker.sendMessageToEhrInOrUnhandled(parsedMessage);

        verify(smallEhrMessagePublisher).sendMessage(messageBody, conversationId);
    }

    @Test
    public void shouldSendLargeEhrMessageToLargeEhrMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        var messageBody = "copc-message";
        var parsedMessage = getMockParsedMessage("RCMR_IN030000UK06", messageBody, conversationId);
        when(parsedMessage.isLargeMessage()).thenReturn(true);
        when(transferStore.isConversationIdPresent(conversationId.toString())).thenReturn(true);

        broker.sendMessageToEhrInOrUnhandled(parsedMessage);
        verify(largeEhrMessagePublisher).sendMessage(messageBody, conversationId);
    }

    @Test
    public void shouldSendNegativeAcknowledgementToNegativeAcknowledgementMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        var messageBody = "nack";
        var acknowledgement = getMockParsedMessage("MCCI_IN010000UK13", messageBody, conversationId, Acknowledgement.class);
        when(acknowledgement.isNegativeAcknowledgement()).thenReturn(true);
        when(transferStore.isConversationIdPresent(conversationId.toString())).thenReturn(true);

        broker.sendMessageToEhrInOrUnhandled(acknowledgement);
        verify(negativeAcknowledgementMessagePublisher).sendMessage("nack", conversationId);
    }

    @Test
    public void shouldSendPositiveAcknowledgementToPositiveAcknowledgementMessagePublisher()  {
        var conversationId = UUID.randomUUID();
        var messageBody = "positive-ack";
        var acknowledgement = getMockParsedMessage("MCCI_IN010000UK13", messageBody, conversationId, Acknowledgement.class);

        when(transferStore.isConversationIdPresent(conversationId.toString())).thenReturn(true);

        broker.sendMessageToEhrInOrUnhandled(acknowledgement);
        verify(positiveAcknowledgementMessagePublisher).sendMessage("positive-ack", conversationId);
    }

    @Test
    public void shouldSendUnreckognizedMessagesToDlq()  {
        var conversationId = UUID.randomUUID();
        var parsedMessage = getMockParsedMessage("something-unreckognizable", "some-ack", conversationId);

        when(transferStore.isConversationIdPresent(conversationId.toString())).thenReturn(true);

        broker.sendMessageToEhrInOrUnhandled(parsedMessage);
        verify(parsingDlqPublisher).sendMessage(any());
    }

    @Test
    public void shouldPublishMessageWithoutKnownConversationIdToEhrInUnhandledTopic() {
        String messageBody = "ehr-request";
        UUID conversationId = UUID.randomUUID();
        var parsedMessage = getMockParsedMessageWithoutInteractionId(messageBody, conversationId);

        when(transferStore.isConversationIdPresent(conversationId.toString())).thenReturn(false);

        broker.sendMessageToEhrInOrUnhandled(parsedMessage);

        verify(ehrInUnhandledMessagePublisher).sendMessage("ehr-request", conversationId);
    }

    @Test
    public void shouldNotPublishMessageWithKnownConversationIdToEhrInUnhandledTopic() {
        String messageBody = "ehr-request";
        UUID conversationId = UUID.randomUUID();
        var parsedMessage = getMockParsedMessage("RCMR_IN030000UK06", messageBody, conversationId);

        when(transferStore.isConversationIdPresent(conversationId.toString())).thenReturn(true);

        broker.sendMessageToEhrInOrUnhandled(parsedMessage);

        verifyNoInteractions(ehrInUnhandledMessagePublisher);
    }
}
