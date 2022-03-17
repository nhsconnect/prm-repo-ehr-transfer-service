package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpmessagemodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SOAPBody {
    @JacksonXmlElementWrapper(localName = "Manifest", namespace = "eb")
    public List<Reference> manifest;
}