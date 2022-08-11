package uk.nhs.prm.repo.ehrtransferservice.timeout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerDb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EhrRequestTimeoutHandlerTest {
    @Mock
    TransferTrackerDb transferTrackerDb;

    @InjectMocks
    EhrRequestTimeoutHandler handler;

    @Test
    void shouldInvokeCallToTransferTrackerDb(){
        handler.handle();
      verify(transferTrackerDb).getTimedOutRecords(any());
    }
}