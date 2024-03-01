package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MessageHeader;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPHeader;
import uk.nhs.prm.repo.ehrtransferservice.models.TransferCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowledgement;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.AcknowledgementTypeCode;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.FailureDetail;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.Transfer;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.nhs.prm.repo.ehrtransferservice.database.TransferState.EHR_TRANSFER_FAILED;

@ExtendWith(MockitoExtension.class)
class NegativeAcknowledgementHandlerTest {

    @Mock
    private TransferService transferService;

    @InjectMocks
    private NegativeAcknowledgementHandler negativeAcknowledgementHandler;

    private final UUID conversationId = UUID.fromString("d263e1ed-95d5-4502-95f8-905c10edc85f");

    private final String nemsMessageId = "8dd4f4be-9449-4efb-b231-e574415dbf86";

    @Test
    void shouldUpdateDbRecordAsTransferFailedUsingCodeFromFirstFailureDetails() {
        // given
        Acknowledgement negativeAcknowledgement = createAcknowledgement(conversationId, failureDetailsList("06", "09"));

        // when
        when(transferService.getNemsMessageIdAsString(conversationId)).thenReturn(nemsMessageId);

        negativeAcknowledgementHandler.handleMessage(negativeAcknowledgement);

        // then
        verify(transferService).updateConversationStatusWithFailure(conversationId, nemsMessageId, EHR_TRANSFER_FAILED, "06");
    }

    @Test
    void shouldUpdateDbRecordAsTransferFailedUsingUnknownErrorCodeIfThereAreNoFailureDetails() {
        // given
        Acknowledgement negativeAcknowledgement = createAcknowledgement(conversationId, noFailureDetails());

        // when
        when(transferService.getNemsMessageIdAsString(conversationId)).thenReturn(nemsMessageId);

        negativeAcknowledgementHandler.handleMessage(negativeAcknowledgement);

        // then
        verify(transferService).updateConversationStatusWithFailure(conversationId, nemsMessageId, EHR_TRANSFER_FAILED, "UNKNOWN_ERROR");
    }

    private List<FailureDetail> noFailureDetails() {
        return failureDetailsList();
    }

    private List<FailureDetail> failureDetailsList(String... failureCodes) {
        List<String> failureCodeList = Arrays.asList(failureCodes);

        return failureCodeList.stream().map(code -> new FailureDetail(null, code, null, null)).collect(toList());
    }

    private Acknowledgement createAcknowledgement(UUID conversationId, List<FailureDetail> failureList) {
        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.header = new SOAPHeader();
        envelope.header.messageHeader = new MessageHeader();
        envelope.header.messageHeader.conversationId = conversationId;
        return new StubAcknowledgement(envelope, failureList);
    }

    public static class StubAcknowledgement extends Acknowledgement {
        private final List<FailureDetail> failures;

        public StubAcknowledgement(SOAPEnvelope envelope, List<FailureDetail> failures) {
            super(envelope, null, null);
            this.failures = failures;
        }

        @Override
        public List<FailureDetail> getFailureDetails() {
            return failures;
        }

        @Override
        public AcknowledgementTypeCode getTypeCode() {
            return AcknowledgementTypeCode.AE;
        }
    }
}
