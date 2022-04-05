package uk.nhs.prm.repo.ehrtransferservice.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.ConversationIdStore;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferTrackerServiceTest {
    @Mock
    TransferTrackerDb transferTrackerDb;
    @Mock
    ConversationIdStore conversationIdStore;
    @InjectMocks
    TransferTrackerService transferTrackerService;
    @Captor
    ArgumentCaptor<TransferTrackerDbEntry> trackerDbEntryArgumentCaptor;

    @Test
    void shouldCallDbWithExpectedValuesForInitialUpdate() {
        when(conversationIdStore.getConversationId()).thenReturn("conversation-Id");

        transferTrackerService.recordEventInDb(createIncomingEvent(), "ACTION:TRANSFER_TO_REPO_STARTED");

        verify(transferTrackerDb).save(trackerDbEntryArgumentCaptor.capture());
        TransferTrackerDbEntry value = trackerDbEntryArgumentCaptor.getValue();
        assertThat(value.getConversationId()).isEqualTo("conversation-Id");
        assertThat(value.getNemsMessageId()).isEqualTo("nems-message-id");
        assertThat(value.getSourceGP()).isEqualTo("source-gp");
        assertThat(value.getNhsNumber()).isEqualTo("123456765");
        assertThat(Instant.parse(value.getDateTime())).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    void shouldCallDbWithExpectedValuesToUpdateWithNewStateAndDateTime() {
        when(conversationIdStore.getConversationId()).thenReturn("conversation-id");

        transferTrackerService.updateStateOfTransfer("ACTION:TRANSFER_TO_REPO_STARTED");

        verify(transferTrackerDb).update(eq("conversation-id"), eq("ACTION:TRANSFER_TO_REPO_STARTED"), any());
    }

    private RepoIncomingEvent createIncomingEvent() {
        return new RepoIncomingEvent("123456765","source-gp","nems-message-id","destination-gp");
    }
}