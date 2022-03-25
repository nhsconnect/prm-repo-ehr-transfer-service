package uk.nhs.prm.repo.ehrtransferservice.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler.ConversationIdGenerator;
import uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.ehrrequesthandler.TransferTrackerDbEntry;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferTrackerServiceTest {
    @Mock
    TransferTrackerDb transferTrackerDb;
    @Mock
    ConversationIdGenerator conversationIdGenerator;
    @InjectMocks
    TransferTrackerService transferTrackerService;
    @Captor
    ArgumentCaptor<TransferTrackerDbEntry> trackerDbEntryArgumentCaptor;

    @Test
    void shouldCallDbWithExpectedValues() {
        when(conversationIdGenerator.getConversationId()).thenReturn("conversation-Id");
        transferTrackerService.recordEventInDb(createIncomingEvent());
        verify(transferTrackerDb).save(trackerDbEntryArgumentCaptor.capture());
        TransferTrackerDbEntry value = trackerDbEntryArgumentCaptor.getValue();
        assertThat(value.getConversationId()).isEqualTo("conversation-Id");
        assertThat(value.getNemsMessageId()).isEqualTo("nems-message-id");
        assertThat(value.getSourceGP()).isEqualTo("source-gp");
        assertThat(value.getNhsNumber()).isEqualTo("123456765");
        assertThat(Instant.parse(value.getDateTime())).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));

    }

    private RepoIncomingEvent createIncomingEvent() {
        HashMap<String, Object> sqsMessage = new HashMap<>();
        sqsMessage.put("nhsNumber", "123456765");
        sqsMessage.put("sourceGP", "source-gp");
        sqsMessage.put("nemsMessageId", "nems-message-id");
        RepoIncomingEvent repoIncomingEvent = new RepoIncomingEvent(sqsMessage);
        return repoIncomingEvent;
    }
}