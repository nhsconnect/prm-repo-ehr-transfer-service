package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

public class EhrLargeMessage extends BaseParsedMessage {

    public EhrLargeMessage(SOAPEnvelope soapEnvelope, MessageContent messageContent, String rawMessage) {
        super(soapEnvelope, rawMessage);
    }

    @Override
    String getNhsNumber() {
        return null;
    }

    @Override
    String getOdsCode() {
        return null;
    }
}