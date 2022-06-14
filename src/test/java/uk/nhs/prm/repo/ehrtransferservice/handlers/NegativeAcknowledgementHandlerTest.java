package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.Gp2gpNackBuilder;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerDb;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.parser_broker.Parser;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NegativeAcknowledgementHandlerTest {

    @InjectMocks
    private NegativeAcknowledgementHandler handler;

    @Mock
    private TransferTrackerDb transferTrackerDb;

    private Parser parser = new Parser();

    @Disabled("WIP")
    @Test
    public void shouldUpdateTransferTrackerRecordWithTransferFailureStatusIncludingErrorDetails() {
        String nack = new Gp2gpNackBuilder()
                .withErrorCode("09")
                .withErrorDisplayText("oh dear :(")
                .build();;

        ParsedMessage parsedNack = parse(nack);

        handler.handleMessage(parsedNack);

        verify(transferTrackerDb, times(1)).update(eq(parsedNack.getConversationId().toString()), eq("ACTION:EHR_TRANSFER_FAILED:09_OH_DEAR"), any());
    }

    private ParsedMessage parse(String messageBody) {
        try {
            return parser.parse(messageBody);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}