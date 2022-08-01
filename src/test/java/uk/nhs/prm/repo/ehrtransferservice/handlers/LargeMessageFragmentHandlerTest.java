package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.EhrCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.EhrCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeEhrMessageFragment;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageResponseBody;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;

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
    TransferTrackerService transferTrackerService;

    @Mock
    EhrCompleteMessagePublisher ehrCompleteMessagePublisher;

    @InjectMocks
    LargeMessageFragmentHandler largeMessageFragmentHandler;

    @Captor
    ArgumentCaptor<LargeEhrMessageFragment> largeMessageFragmentsArgumentCaptor;

    private UUID conversationId;
    private UUID messageId;

    @Test
    void shouldCallEhrRepoServiceToStoreTheMessage() throws Exception {
        setUp();
        when(ehrRepoService.storeMessage(largeMessageFragmentsArgumentCaptor.capture())).thenReturn(new StoreMessageResponseBody("complete"));
        largeMessageFragmentHandler.handleMessage(largeSqsMessage);
        verify(ehrRepoService).storeMessage(largeMessageFragmentsArgumentCaptor.capture());
        assertThat(largeMessageFragmentsArgumentCaptor.getValue().getNhsNumber()).isEqualTo("");
    }

    @Test
    void shouldCallTransferTrackerDBServiceWithTheConversationId() throws Exception {
        setUp();
        when(ehrRepoService.storeMessage(largeMessageFragmentsArgumentCaptor.capture())).thenReturn(new StoreMessageResponseBody("complete"));
        largeMessageFragmentHandler.handleMessage(largeSqsMessage);
        verify(transferTrackerService).getEhrTransferData(largeSqsMessage.getConversationId().toString());
    }

    @Test
    void shouldPostMessageToEhrCompleteQueueIfStoredResponseStatusIsComplete() throws Exception {
        setUp();
        var ehrCompleteEvent = new EhrCompleteEvent(conversationId, messageId);

        when(ehrRepoService.storeMessage(largeMessageFragmentsArgumentCaptor.capture())).thenReturn(new StoreMessageResponseBody("complete"));
        largeMessageFragmentHandler.handleMessage(largeSqsMessage);

        verify(ehrCompleteMessagePublisher).sendMessage(ehrCompleteEvent);
    }

    @Test
    void shouldNotPostMessageToEhrCompleteQueueIfStoredResponseStatusIsNotEqualToComplete() throws Exception {
        when(ehrRepoService.storeMessage(largeMessageFragmentsArgumentCaptor.capture())).thenReturn(new StoreMessageResponseBody("not complete"));
        largeMessageFragmentHandler.handleMessage(largeSqsMessage);

        verify(ehrCompleteMessagePublisher, times(0)).sendMessage(any());
    }

    private TransferTrackerDbEntry getTransferTrackerDbEntry() {
        return new TransferTrackerDbEntry(conversationId.toString(),"some-nhs-number","some-source-gp","some-nems-message-id","last-updated","some-state","some-date",messageId.toString());
    }

    void setUp() {
        conversationId = UUID.randomUUID();
        messageId = UUID.randomUUID();

        when(largeSqsMessage.getConversationId()).thenReturn(conversationId);
        when(transferTrackerService.getEhrTransferData(conversationId.toString())).thenReturn(getTransferTrackerDbEntry());
    }
}