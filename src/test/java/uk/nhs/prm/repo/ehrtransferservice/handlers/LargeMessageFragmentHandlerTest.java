package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferStore;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.EhrCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeEhrFragmentMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageResponseBody;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.Transfer;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.StoreMessageResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LargeMessageFragmentHandlerTest {
    @Mock
    LargeSqsMessage largeSqsMessage;

    @Mock
    EhrRepoService ehrRepoService;

    @Mock
    TransferStore transfers;

    @Mock
    EhrCompleteMessagePublisher ehrCompleteMessagePublisher;

    @InjectMocks
    LargeMessageFragmentHandler largeMessageFragmentHandler;

    @Captor
    ArgumentCaptor<LargeEhrFragmentMessage> largeMessageFragmentsArgumentCaptor;

    private UUID conversationId;
    private UUID ehrCoreMessageId;

    @BeforeEach
    public void setUpMessageIds() {
        conversationId = UUID.randomUUID();
        ehrCoreMessageId = UUID.randomUUID();

        lenient().when(largeSqsMessage.getConversationId()).thenReturn(conversationId);
    }

    @Test
    void shouldCallEhrRepoServiceToStoreTheMessage() throws Exception {
        when(transfers.findTransfer(conversationId)).thenReturn(aTransfer(conversationId, ehrCoreMessageId));
        when(ehrRepoService.storeMessage(largeMessageFragmentsArgumentCaptor.capture())).thenReturn(new StoreMessageResult(new StoreMessageResponseBody("complete")));
        largeMessageFragmentHandler.handleMessage(largeSqsMessage);
        verify(ehrRepoService).storeMessage(largeMessageFragmentsArgumentCaptor.capture());
        assertThat(largeMessageFragmentsArgumentCaptor.getValue().getNhsNumber()).isEqualTo("");
    }

    @Test
    void shouldFindTransferByItsConversationId() throws Exception {
        when(transfers.findTransfer(conversationId)).thenReturn(aTransfer(conversationId, ehrCoreMessageId));

        when(ehrRepoService.storeMessage(largeMessageFragmentsArgumentCaptor.capture())).thenReturn(new StoreMessageResult(new StoreMessageResponseBody("complete")));

        largeMessageFragmentHandler.handleMessage(largeSqsMessage);

        verify(transfers).findTransfer(largeSqsMessage.getConversationId());
    }

    @Test
    void shouldPostMessageToEhrCompleteQueueWithEhrCoreMessageIdIfStoredResponseStatusIsComplete() throws Exception {
        when(transfers.findTransfer(conversationId)).thenReturn(aTransfer(conversationId, ehrCoreMessageId));

        var ehrCompleteEvent = new EhrCompleteEvent(conversationId, ehrCoreMessageId);

        when(ehrRepoService.storeMessage(largeMessageFragmentsArgumentCaptor.capture())).thenReturn(new StoreMessageResult(new StoreMessageResponseBody("complete")));
        largeMessageFragmentHandler.handleMessage(largeSqsMessage);

        verify(ehrCompleteMessagePublisher).sendMessage(ehrCompleteEvent);
    }

    @Test
    void shouldNotPostMessageToEhrCompleteQueueIfStoredResponseStatusIsNotEqualToComplete() throws Exception {
        when(ehrRepoService.storeMessage(largeMessageFragmentsArgumentCaptor.capture())).thenReturn(new StoreMessageResult(new StoreMessageResponseBody("not complete")));

        largeMessageFragmentHandler.handleMessage(largeSqsMessage);

        verify(ehrCompleteMessagePublisher, times(0)).sendMessage(any());
    }

    private Transfer aTransfer(UUID conversationId, UUID ehrCoreMessageId) {
        return new Transfer(conversationId.toString(),
                "some-nhs-number",
                "some-source-gp",
                "some-nems-message-id",
                "last-updated",
                "some-state",
                "some-date",
                "some-last-updated-date",
                ehrCoreMessageId.toString(),
                true);
    }
}