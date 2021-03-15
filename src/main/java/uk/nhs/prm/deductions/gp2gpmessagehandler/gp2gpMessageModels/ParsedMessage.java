package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels;

public class ParsedMessage {
    private SOAPEnvelope soapEnvelope;

    public ParsedMessage(SOAPEnvelope soapEnvelope) {
        this.soapEnvelope = soapEnvelope;
    }

    public String getAction() {
        if (soapEnvelope.header == null || soapEnvelope.header.messageHeader == null) {
            return null;
        }
        return soapEnvelope.header.messageHeader.action;
    }

    public String getConversationId() {
        if (soapEnvelope.header == null || soapEnvelope.header.messageHeader == null) {
            return null;
        }
        return soapEnvelope.header.messageHeader.conversationId;
    }

    public String getMessageId() {
        if (soapEnvelope.header == null || soapEnvelope.header.messageHeader == null || soapEnvelope.header.messageHeader.messageData == null) {
            return null;
        }
        return soapEnvelope.header.messageHeader.messageData.messageId;
    }

    public boolean isLargeMessage() {
        for (Reference reference: soapEnvelope.body.manifest) {
            if (reference.href.contains("mid")) {
                return true;
            }
        }
        return false;
    }
}
