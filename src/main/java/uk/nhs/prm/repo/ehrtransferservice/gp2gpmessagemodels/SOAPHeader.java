package uk.nhs.prm.repo.ehrtransferservice.gp2gpmessagemodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SOAPHeader {
    @JacksonXmlProperty(localName = "MessageHeader", namespace = "eb")
    public MessageHeader messageHeader;
}