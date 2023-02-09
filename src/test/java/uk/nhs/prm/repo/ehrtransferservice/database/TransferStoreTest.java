package uk.nhs.prm.repo.ehrtransferservice.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.TransferTrackerDbException;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.SplunkAuditPublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.SplunkAuditMessage;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.Transfer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferStoreTest {
    @Mock
    TransferTrackerDb transferTrackerDb;
    @InjectMocks
    TransferStore transferStore;
    @Captor
    ArgumentCaptor<Transfer> trackerDbEntryArgumentCaptor;
    @Mock
    SplunkAuditPublisher splunkAuditPublisher;

    @Test
    void shouldCallDbWithExpectedValuesForInitialSaveInDb() {
        transferStore.createEhrTransfer(createIncomingEvent(), "ACTION:TRANSFER_TO_REPO_STARTED");

        verify(transferTrackerDb).save(trackerDbEntryArgumentCaptor.capture());
        Transfer value = trackerDbEntryArgumentCaptor.getValue();
        assertThat(value.getConversationId()).isEqualTo("conversation-id");
        assertThat(value.getNemsMessageId()).isEqualTo("nems-message-id");
        assertThat(value.getSourceGP()).isEqualTo("source-gp");
        assertThat(value.getNhsNumber()).isEqualTo("123456765");
        assertThat(value.getNemsEventLastUpdated()).isEqualTo("last-updated");
        assertThat(value.getLargeEhrCoreMessageId()).isEqualTo("");
        assertThat(value.getIsActive()).isEqualTo(true);
        assertThat(Instant.parse(value.getLastUpdatedAt())).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(Instant.parse(value.getCreatedAt())).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    void shouldThrowExceptionWhenFailsToMakeInitialSaveInDb() {
        doThrow(RuntimeException.class).when(transferTrackerDb).save(trackerDbEntryArgumentCaptor.capture());

        assertThrows(TransferTrackerDbException.class, () -> transferStore.createEhrTransfer(createIncomingEvent(), "ACTION:TRANSFER_TO_REPO_STARTED"));
    }

    @Test
    void shouldCallDbWithExpectedValuesToUpdateWithNewInputs() {
        transferStore.handleEhrTransferStateUpdate("conversation-id", "nems-message-id", "ACTION:TRANSFER_TO_REPO_STARTED", true);
        verify(splunkAuditPublisher).sendMessage(new SplunkAuditMessage("conversation-id", "nems-message-id", "ACTION:TRANSFER_TO_REPO_STARTED"));

        verify(transferTrackerDb).update(eq("conversation-id"), eq("ACTION:TRANSFER_TO_REPO_STARTED"), any(), eq(true));
    }

    @Test
    void shouldThrowExceptionWhenFailsToUpdateWithNewStateAndLastUpdatedAt() {
        doThrow(RuntimeException.class).when(transferTrackerDb).update(eq("conversation-id"), eq("ACTION:TRANSFER_TO_REPO_STARTED"), any(), eq(true));

        assertThrows(TransferTrackerDbException.class, () -> transferStore.handleEhrTransferStateUpdate("conversation-id", "some-nems", "ACTION:TRANSFER_TO_REPO_STARTED", true));
    }

    @Test
    void shouldCallDbWithExpectedValuesToUpdateWithNewLargeEhrCoreMessageId() {
        transferStore.updateLargeEhrCoreMessageId("conversation-id","large-ehr-core-message-id");

        verify(transferTrackerDb).updateLargeEhrCoreMessageId(eq("conversation-id"), eq("large-ehr-core-message-id"));
    }

    @Test
    void shouldThrowExceptionWhenFailsToUpdateWithNewLargeEhrCoreMessageId() {
        doThrow(RuntimeException.class).when(transferTrackerDb).updateLargeEhrCoreMessageId(eq("conversation-id"), eq("large-ehr-core-message-id"));

        assertThrows(TransferTrackerDbException.class, () -> transferStore.updateLargeEhrCoreMessageId("conversation-id","large-ehr-core-message-id"));
    }

    @Test
    void shouldGetDbInformationForSpecifiedConversationId() {
        var conversationId = "conversationid";
        var dbEntry = new Transfer(conversationId, "", "", "", "", "", "","", "", true);
        when(transferTrackerDb.getByConversationId(conversationId)).thenReturn(dbEntry);
        transferStore.findTransfer(conversationId);

        verify(transferTrackerDb).getByConversationId(conversationId);
    }

    @Test
    void shouldThrowExceptionWhenFailsToGetDbInformationForSpecifiedConversationId() {
        when(transferTrackerDb.getByConversationId(eq("conversation-id"))).thenReturn(null);

        assertThrows(TransferTrackerDbException.class, () -> transferStore.findTransfer("conversation-id"));
    }

    @Test
    void shouldReturnFalseWhenConversationIdIsNotPresentInDatabase() {
        when(transferTrackerDb.getByConversationId("not-present-conversation-id")).thenReturn(null);
        boolean conversationIdPresent = transferStore.isConversationIdPresent("not-present-conversation-id");

        verify(transferTrackerDb).getByConversationId("not-present-conversation-id");
        assertThat(conversationIdPresent).isFalse();
    }

    @Test
    void shouldReturnTrueWhenConversationIdIsPresentInDatabase() {
        var conversationId = "conversationid";
        var dbEntry = new Transfer(conversationId, "", "", "", "", "", "","", "", true);
        when(transferTrackerDb.getByConversationId(conversationId)).thenReturn(dbEntry);
        boolean conversationIdPresent = transferStore.isConversationIdPresent(conversationId);

        verify(transferTrackerDb).getByConversationId(conversationId);
        assertThat(conversationIdPresent).isTrue();
    }

    private RepoIncomingEvent createIncomingEvent() {
        return new RepoIncomingEvent("123456765","source-gp","nems-message-id","destination-gp", "last-updated", "conversation-id");
    }
}