package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import javax.xml.bind.annotation.XmlAttribute;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Patient {
    @JacksonXmlProperty(localName = "id")
    public PatientId id;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PatientId {
        @XmlAttribute(name = "extension")
        public String extension;
    }
}
