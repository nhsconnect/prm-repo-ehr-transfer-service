package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferStore;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferTrackerDbException;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.TransferCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.models.TransferCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.Transfer;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EhrCompleteHandlerTest {

    @Mock
    EhrCompleteEvent ehrCompleteEvent;

    @Mock
    TransferStore transferStore;

    @Mock
    Gp2gpMessengerService gp2gpMessengerService;

    Transfer transfer;

    @Mock
    TransferCompleteMessagePublisher transferCompleteMessagePublisher;

    @InjectMocks
    EhrCompleteHandler ehrCompleteHandler;

    UUID conversationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        transfer = new Transfer("conversationId", "some-nhs-number",
                "some-ods-code", "some-nems-message-id", "nemsEventLastUpdated",
                "state", "createdAt", "lastUpdatedAt", "largeEhrCoreMessageId", true);
        when(ehrCompleteEvent.getConversationId()).thenReturn(conversationId);
        when(transferStore.findTransfer(conversationId.toString())).thenReturn(transfer);
    }

    @Test
    void shouldCallGp2gpMessengerServiceToSendPositiveAcknowledgement() throws Exception {
        ehrCompleteHandler.handleMessage(ehrCompleteEvent);
        verify(gp2gpMessengerService).sendEhrCompletePositiveAcknowledgement(ehrCompleteEvent, transfer);
    }

    @Test
    void shouldUpdateDbWithEhrTransferStatusWhenEhrRequestSentSuccessfully() throws Exception {
        ehrCompleteHandler.handleMessage(ehrCompleteEvent);
        verify(transferStore).handleEhrTransferStateUpdate(conversationId.toString(), "some-nems-message-id", "ACTION:EHR_TRANSFER_TO_REPO_COMPLETE", false);
    }

    @Test
    void shouldThrowErrorAndNotUpdateDbWhenFailsToSendPositiveAcknowledgement() throws Exception {
        doThrow(Exception.class).when(gp2gpMessengerService).sendEhrCompletePositiveAcknowledgement(ehrCompleteEvent, transfer);

        assertThrows(Exception.class, () -> ehrCompleteHandler.handleMessage(ehrCompleteEvent));
        verify(transferStore, never()).handleEhrTransferStateUpdate(conversationId.toString(),"some-nems-message-id","ACTION:EHR_TRANSFER_TO_REPO_COMPLETE", false);
    }

    @Test
    void shouldPublishTransferCompleteMessageToTransferCompleteTopic() throws Exception {
        var transferComplete = new TransferCompleteEvent("nemsEventLastUpdated", "some-ods-code", "SUSPENSION", "some-nems-message-id", "some-nhs-number");
        ehrCompleteHandler.handleMessage(ehrCompleteEvent);
        verify(transferCompleteMessagePublisher).sendMessage(transferComplete, conversationId);
    }

    @Test
    void shouldThrowErrorAndNotSendMessageWhenDbFailsTOUpdate() {
        doThrow(TransferTrackerDbException.class).when(transferStore).handleEhrTransferStateUpdate(conversationId.toString(),"nemsMessageId","ACTION:EHR_TRANSFER_TO_REPO_COMPLETE", false);

        assertThrows(Exception.class, () -> ehrCompleteHandler.handleMessage(ehrCompleteEvent));
        verify(transferCompleteMessagePublisher, never()).sendMessage(any(), any());
    }

}