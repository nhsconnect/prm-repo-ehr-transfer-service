package uk.nhs.prm.repo.ehrtransferservice.models;

import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MessageContent;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.FailureDetail;

import java.util.ArrayList;
import java.util.List;

public class LargeSqsMessage extends ParsedMessage {
    public LargeSqsMessage(SOAPEnvelope soapEnvelope, MessageContent messageContent, String rawMessage) {
        super(soapEnvelope, messageContent, rawMessage);
    }

    public List<FailureDetail> getFailureDetails() {
        return new ArrayList<>();
    }
}
