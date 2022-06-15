package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MessageHeader;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPHeader;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NegativeAcknowledgementHandlerTest {

    @Mock
    TransferTrackerService transferTrackerService;

    @InjectMocks
    NegativeAcknowledgementHandler negativeAcknowledgementHandler;

    private final UUID conversationId = UUID.randomUUID();

    @Test
    void shouldUpdateDbRecordAsTransferFailed(){
        negativeAcknowledgementHandler.handleMessage(createParsedMessage());
        verify(transferTrackerService, times(1)).updateStateOfEhrTransfer(conversationId.toString(),
                "ACTION:EHR_TRANSFER_FAILED:${nack-error-here}");

    }

    ParsedMessage createParsedMessage(){
        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.header = new SOAPHeader();
        envelope.header.messageHeader = new MessageHeader();
        envelope.header.messageHeader.conversationId= conversationId;
        return new ParsedMessage(envelope, null, null);
    }



}
