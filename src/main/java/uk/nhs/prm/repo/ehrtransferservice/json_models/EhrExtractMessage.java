package uk.nhs.prm.repo.ehrtransferservice.json_models;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.UUID;

public class EhrExtractMessage {

    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public UUID messageId;

    public EhrExtractMessage(UUID messageId) {
        this.messageId = messageId;
    }
}
