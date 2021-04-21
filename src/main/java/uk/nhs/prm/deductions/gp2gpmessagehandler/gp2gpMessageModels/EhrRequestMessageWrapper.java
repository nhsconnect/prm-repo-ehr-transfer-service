package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrRequestMessageWrapper extends MessageContent {
    @JacksonXmlProperty(localName = "ControlActEvent")
    public ControlActEvent controlActEvent;

    public EhrRequest getEhrRequest() {
        return this.controlActEvent.subject.ehrRequest;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ControlActEvent {
        @JacksonXmlProperty(localName = "subject")
        public Subject subject;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Subject {

            @JacksonXmlProperty(localName = "EhrRequest")
            public EhrRequest ehrRequest;
        }
    }
}
