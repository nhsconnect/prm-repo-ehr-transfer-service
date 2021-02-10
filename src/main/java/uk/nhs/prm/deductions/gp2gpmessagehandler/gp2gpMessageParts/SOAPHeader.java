package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageParts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SOAPHeader {
    @JacksonXmlProperty(localName = "MessageHeader", namespace = "eb")
    public MessageHeader messageHeader = new MessageHeader();

    @Override
    public String toString() {
        return "SOAPHeader{" +
                "messageHeader=" + messageHeader +
                '}';
    }
}