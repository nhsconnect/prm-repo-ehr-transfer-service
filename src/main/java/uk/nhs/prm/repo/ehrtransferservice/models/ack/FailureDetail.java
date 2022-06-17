package uk.nhs.prm.repo.ehrtransferservice.models.ack;

import lombok.AllArgsConstructor;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.AcknowledgementMessageWrapper;
import uk.nhs.prm.repo.ehrtransferservice.models.FailureLevel;

@AllArgsConstructor
public class FailureDetail {
    private final String displayName;
    private final String code;
    private final String codeSystem;
    private final FailureLevel level;

    public FailureDetail(AcknowledgementMessageWrapper.ControlActEvent.Reason reason) {
        displayName = reason.getErrorDisplayName();
        code = reason.getCode();
        codeSystem = reason.getCodeSystem();
        level = FailureLevel.parse(reason.getQualifier());
    }

    public FailureDetail(AcknowledgementMessageWrapper.Acknowledgement.AcknowledgementDetail detail) {
        displayName = null;
        code = null;
        codeSystem = null;
        level = null;
    }

    public String displayName() {
        return displayName;
    }

    public String code() {
        return code;
    }

    public String codeSystem() {
        return codeSystem;
    }

    public FailureLevel level() {
        return level;
    }
}
