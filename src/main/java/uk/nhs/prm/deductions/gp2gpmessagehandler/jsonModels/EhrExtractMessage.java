package uk.nhs.prm.deductions.gp2gpmessagehandler.jsonModels;

import java.util.UUID;

public class EhrExtractMessage {

    public UUID ehrExtractMessageId;

    public EhrExtractMessage(UUID ehrExtractMessageId) {
        this.ehrExtractMessageId = ehrExtractMessageId;
    }
}
