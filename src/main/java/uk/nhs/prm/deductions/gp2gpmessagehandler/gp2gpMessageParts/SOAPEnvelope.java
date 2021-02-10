package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageParts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SOAPEnvelope {
    @JacksonXmlProperty(localName = "Header", namespace = "SOAP-ENV")
    public SOAPHeader header = new SOAPHeader();

    @Override
    public String toString() {
        return "SOAPEnvelope{" +
                "header=" + header +
                '}';
    }
}
