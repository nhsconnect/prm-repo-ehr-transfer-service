package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrExtractMessageWrapper {
    @JacksonXmlProperty(localName = "ControlActEvent")
    public ControlActEvent controlActEvent;

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
