package uk.nhs.prm.repo.ehrtransferservice.models;

import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MessageContent;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;

public class LargeEhrMessage extends ParsedMessage {
    public LargeEhrMessage(SOAPEnvelope soapEnvelope, MessageContent messageContent, String rawMessage) {
        super(soapEnvelope, messageContent, rawMessage);
    }
}
