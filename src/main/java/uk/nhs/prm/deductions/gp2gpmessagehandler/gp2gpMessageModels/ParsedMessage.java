package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels;

import static net.logstash.logback.argument.StructuredArguments.v;

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
    //TODO: soap body, which effectively has the MIDs
}
