package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.FailureDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AcknowledgementMessageWrapper extends MessageContent {

    public Acknowledgement acknowledgement;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Acknowledgement {
        public AcknowledgementDetail acknowledgementDetail;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class AcknowledgementDetail {

        }
    }

    @JacksonXmlProperty(localName = "ControlActEvent")
    public ControlActEvent controlActEvent;

    public List<String> getReasons() {
        if (controlActEvent.reason != null) {
            return reasons().map(r -> r.getErrorDisplayName()).collect(toList());
        }
        return new ArrayList<>();
    }

    public List<FailureDetail> getFailureDetails() {
        return reasons().map(r -> new FailureDetail(r)).collect(toList());
    }

    private Stream<ControlActEvent.Reason> reasons() {
        return controlActEvent.reason.stream();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ControlActEvent {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Reason> reason;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Reason {

            public JustifyingDetectedIssueEvent justifyingDetectedIssueEvent;

            public String getErrorDisplayName() {
                return hasEventCode() ? eventCode().displayName : null;
            }

            public String getCode() {
                return hasEventCode() ? eventCode().code : null;
            }

            public String getCodeSystem() {
                return hasEventCode() ? eventCode().codeSystem : null;
            }

            public String getQualifier() {
                if (!hasEventCode()) {
                    return null;
                }
                if (eventCode().qualifier == null) {
                    return null;
                }
                return eventCode().qualifier.code;
            }

            private boolean hasEventCode() {
                return justifyingDetectedIssueEvent != null && eventCode() != null;
            }

            private JustifyingDetectedIssueEvent.Code eventCode() {
                return justifyingDetectedIssueEvent.code;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class JustifyingDetectedIssueEvent {
                public Code code;

                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Code {

                    public String displayName;
                    public String code;
                    public String codeSystem;
                    public Qualifier qualifier;

                    public static class Qualifier {
                        public String code;
                    }
                }
            }
        }
    }
}
