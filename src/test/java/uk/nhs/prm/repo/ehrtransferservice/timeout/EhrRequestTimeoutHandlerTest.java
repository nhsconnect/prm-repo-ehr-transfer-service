package uk.nhs.prm.repo.ehrtransferservice.timeout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.config.Tracer;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerDb;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.TransferCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.TransferCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class EhrRequestTimeoutHandlerTest {
    @Mock
    TransferTrackerDb transferTrackerDb;
    @Mock
    Tracer tracer;

    @Mock
    TransferCompleteMessagePublisher transferCompleteMessagePublisher;

    @InjectMocks
    EhrRequestTimeoutHandler handler;

    UUID conversationId = UUID.randomUUID();
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
        setField(handler, "timeoutInSeconds", "1");
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
        verify(transferTrackerDb).update(eq(conversationId.toString()),eq("ACTION:EHR_TRANSFER_TIMEOUT"), any(),eq(false));
    }

    @Test
    void shouldSendTimedOutRecordsToTransferCompleteQueue(){
        var transferCompleteEvent = new TransferCompleteEvent(nemsEventLastUpdated, sourceGP, "SUSPENSION", nemsMessageId, nhsNumber);
        when(transferTrackerDb.getTimedOutRecords(any())).thenReturn(listOfTimedOutRecords());
        handler.handle();
        verify(transferCompleteMessagePublisher).sendMessage(transferCompleteEvent, conversationId);
    }

    private List<TransferTrackerDbEntry> listOfTimedOutRecords() {
        List<TransferTrackerDbEntry> records = new ArrayList<>();
        var dbEntry = new TransferTrackerDbEntry(conversationId.toString(), nhsNumber, sourceGP, nemsMessageId, nemsEventLastUpdated, state, createdAt, lastUpdatedAt, largeEhrCoreMessageId, active);
        records.add(dbEntry);
        return records;
    }

}