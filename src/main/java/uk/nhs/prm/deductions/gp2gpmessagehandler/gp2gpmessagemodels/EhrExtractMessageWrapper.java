package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpmessagemodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrExtractMessageWrapper extends MessageContent {
    @JacksonXmlProperty(localName = "ControlActEvent")
    public ControlActEvent controlActEvent;

    public EhrExtract getEhrExtract() {
        return this.controlActEvent.subject.ehrExtract;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ControlActEvent {
        @JacksonXmlProperty(localName = "subject")
        public Subject subject;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Subject {

            @JacksonXmlProperty(localName = "EhrExtract")
            public EhrExtract ehrExtract;
        }
    }
}
