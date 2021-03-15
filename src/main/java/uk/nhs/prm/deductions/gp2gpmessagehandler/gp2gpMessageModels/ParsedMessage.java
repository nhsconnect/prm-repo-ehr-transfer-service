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

    public boolean isLargeMessage() {
        for (Reference reference: soapEnvelope.body.manifest) {
            if (reference.href.contains("mid")) {
                return true;
            }
        }
        return false;
    }
}
