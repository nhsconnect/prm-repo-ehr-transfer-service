package uk.nhs.prm.repo.ehrtransferservice.models.ack;

import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.AcknowledgementMessageWrapper;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MessageContent;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;

import java.util.List;

public class Acknowlegement extends ParsedMessage {
    public Acknowlegement(SOAPEnvelope soapEnvelope, MessageContent messageContent, String rawMessage) {
        super(soapEnvelope, messageContent, rawMessage);
    }

    public List<FailureDetail> getFailureDetails() {
        return acknowledgementContent().getFailureDetails();
    }

    private AcknowledgementMessageWrapper acknowledgementContent() {
        return (AcknowledgementMessageWrapper) getMessageContent();
    }

}
