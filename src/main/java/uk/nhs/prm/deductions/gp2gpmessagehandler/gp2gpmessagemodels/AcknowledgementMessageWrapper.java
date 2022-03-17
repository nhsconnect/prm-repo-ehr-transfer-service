package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpmessagemodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AcknowledgementMessageWrapper extends MessageContent {
    @JacksonXmlProperty(localName = "ControlActEvent")
    public ControlActEvent controlActEvent;

    public List<String> getReasons() {
        if(controlActEvent.reason != null) {
            return controlActEvent.reason.stream().map(ControlActEvent.Reason::getErrorDisplayName)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ControlActEvent {
        @JacksonXmlProperty(localName = "reason")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Reason> reason;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Reason {

            @JacksonXmlProperty(localName = "justifyingDetectedIssueEvent")
            public JustifyingDetectedIssueEvent justifyingDetectedIssueEvent;

            public String getErrorDisplayName() {
                if(justifyingDetectedIssueEvent != null && justifyingDetectedIssueEvent.code != null) {
                    return justifyingDetectedIssueEvent.code.displayName;
                }
                return null;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class JustifyingDetectedIssueEvent {

                @JacksonXmlProperty(localName = "code")
                public Code code;

                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Code {

                    @JacksonXmlProperty(localName = "displayName")
                    public String displayName;
                }
            }
        }
    }
}
