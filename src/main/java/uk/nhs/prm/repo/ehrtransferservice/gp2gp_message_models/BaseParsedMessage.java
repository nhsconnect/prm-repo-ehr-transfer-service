package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import java.util.UUID;

public abstract class BaseParsedMessage {

    private final SOAPEnvelope soapEnvelope;
    private final String rawMessage;

    public BaseParsedMessage(SOAPEnvelope soapEnvelope, String rawMessage) {
        this.soapEnvelope = soapEnvelope;
        this.rawMessage = rawMessage;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public SOAPEnvelope getSoapEnvelope() {
        return soapEnvelope;
    }

    abstract String getNhsNumber();

    public String getInteractionId() {
        SOAPEnvelope soapEnvelope = getSoapEnvelope();
        if (soapEnvelope.header == null || soapEnvelope.header.messageHeader == null) {
            return null;
        }
        return soapEnvelope.header.messageHeader.action;
    }

    public UUID getConversationId() {
        SOAPEnvelope soapEnvelope = getSoapEnvelope();
        if (soapEnvelope.header == null || soapEnvelope.header.messageHeader == null) {
            return null;
        }
        return soapEnvelope.header.messageHeader.conversationId;
    }

    public UUID getMessageId() {
        SOAPEnvelope soapEnvelope = getSoapEnvelope();
        if (soapEnvelope.header == null || soapEnvelope.header.messageHeader == null || soapEnvelope.header.messageHeader.messageData == null) {
            return null;
        }
        return soapEnvelope.header.messageHeader.messageData.messageId;
    }

    abstract String getOdsCode();
}
