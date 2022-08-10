package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferTrackerService;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MessageHeader;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPHeader;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.TransferCompleteMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.models.TransferCompleteEvent;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowledgement;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.AcknowledgementTypeCode;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.FailureDetail;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.TransferTrackerDbEntry;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NegativeAcknowledgementHandlerTest {

    @Mock
    TransferTrackerService transferTrackerService;

    @Mock
    TransferCompleteMessagePublisher transferCompleteMessagePublisher;

    @InjectMocks
    NegativeAcknowledgementHandler negativeAcknowledgementHandler;

    private final UUID conversationId = UUID.randomUUID();

    @Test
    void shouldUpdateDbRecordAsTransferFailedUsingCodeFromFirstFailureDetails() throws Exception {
        TransferTrackerDbEntry transferTrackerDbEntry =
                new TransferTrackerDbEntry(
                        conversationId.toString(),
                        "1234567890",
                        "sourceGP",
                        "someNemsMessageId",
                        "yesterday",
                        "FAILED",
                        null,
                        null,
                        null,
                        true
                );
        TransferCompleteEvent transferCompleteEvent =
                new TransferCompleteEvent("yesterday",
                        "sourceGP",
                        "SUSPENSION",
                        "someNemsMessageId",
                        "1234567890");

        when(transferTrackerService.getEhrTransferData(conversationId.toString())).thenReturn(transferTrackerDbEntry);

        negativeAcknowledgementHandler.handleMessage(createAcknowledgement(conversationId, failureDetailsList("06", "09")));

        verify(transferTrackerService, times(1)).handleEhrTransferStateUpdate(conversationId.toString(),
                "someNemsMessageId" , "ACTION:EHR_TRANSFER_FAILED:06", false);

        verify(transferCompleteMessagePublisher, times(1)).sendMessage(transferCompleteEvent, conversationId);
    }

    @Test
    void shouldUpdateDbRecordAsTransferFailedUsingUnknownErrorCodeIfThereAreNoFailureDetails() throws Exception {
        TransferTrackerDbEntry transferTrackerDbEntry =
                new TransferTrackerDbEntry(
                        conversationId.toString(),
                        "1234567890",
                        "sourceGP",
                        "someNemsMessageId",
                        "yesterday",
                        "FAILED",
                        null,
                        null,
                        null,
                        true
                );

        TransferCompleteEvent transferCompleteEvent =
                new TransferCompleteEvent("yesterday",
                        "sourceGP",
                        "SUSPENSION",
                        "someNemsMessageId",
                        "1234567890");

        when(transferTrackerService.getEhrTransferData(conversationId.toString())).thenReturn(transferTrackerDbEntry);

        negativeAcknowledgementHandler.handleMessage(createAcknowledgement(conversationId, noFailureDetails()));

        verify(transferTrackerService, times(1)).handleEhrTransferStateUpdate(conversationId.toString(),
                "someNemsMessageId" ,"ACTION:EHR_TRANSFER_FAILED:UNKNOWN_ERROR", false);
        verify(transferCompleteMessagePublisher, times(1)).sendMessage(transferCompleteEvent, conversationId);
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
