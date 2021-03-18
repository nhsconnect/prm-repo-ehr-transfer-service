package uk.nhs.prm.deductions.gp2gpmessagehandler.jsonModels;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;

public class EhrExtractMessage {

    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public UUID ehrExtractMessageId;

    public EhrExtractMessage(UUID ehrExtractMessageId) {
        this.ehrExtractMessageId = ehrExtractMessageId;
    }
}
