package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrExtract {
    @JacksonXmlProperty(localName = "recordTarget")
    public RecordTarget recordTarget;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecordTarget {
        @JacksonXmlProperty(localName = "patient")
        public Patient patient;
    }
}
