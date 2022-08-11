package uk.nhs.prm.repo.ehrtransferservice.timeout;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerDb;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static com.github.jknack.handlebars.helper.ConditionalHelpers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class EhrRequestTimeoutHandlerTest {
    @Mock
    TransferTrackerDb transferTrackerDb;

    @InjectMocks
    EhrRequestTimeoutHandler handler;

    String conversationId = "conversation Id";
    String nhsNumber = "111111111";
    String sourceGP = "source gp";
    String nemsMessageId = "Nems message Id";
    String nemsEventLastUpdated = "Last updated";
    String state = "state";
    String createdAt = "2017-11-01T15:00:33+00:00";
    String lastUpdatedAt = "2017-11-01T15:00:33+00:00";
    String largeEhrCoreMessageId = "large ehr core message Id";
    Boolean active = true;


    @BeforeEach
    void setUp() {
        setField(handler, "timeout", 1);
    }

    @Test
    void shouldInvokeCallToTransferTrackerDb() {
        when(transferTrackerDb.getTimedOutRecords(any())).thenReturn(listOfTimedOutRecords());
        handler.handle();
        verify(transferTrackerDb).getTimedOutRecords(any());
    }

    @Test
    void shouldUpdateTransferTrackerDbWithTimeoutStatus(){
        when(transferTrackerDb.getTimedOutRecords(any())).thenReturn(listOfTimedOutRecords());
        handler.handle();
        verify(transferTrackerDb).update(eq(conversationId),eq("ACTION:EHR_TRANSFER_TIMEOUT"), any(),eq(false));
    }

    private List<TransferTrackerDbEntry> listOfTimedOutRecords() {

        List<TransferTrackerDbEntry> records = new ArrayList<>();
        records.add(new TransferTrackerDbEntry(conversationId, nhsNumber, sourceGP, nemsMessageId, nemsEventLastUpdated, state, createdAt, lastUpdatedAt, largeEhrCoreMessageId, active));
        return records;
    }
}