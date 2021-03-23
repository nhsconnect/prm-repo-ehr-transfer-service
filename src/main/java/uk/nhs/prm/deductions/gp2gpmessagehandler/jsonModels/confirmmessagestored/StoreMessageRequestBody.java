package uk.nhs.prm.deductions.gp2gpmessagehandler.jsonModels.confirmmessagestored;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;
import java.util.UUID;

public class StoreMessageRequestBody {

    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public StoreMessageData data;

    public StoreMessageRequestBody(UUID messageId, UUID conversationId, String nhsNumber, String messageType, List<UUID> attachmentMessageIds) {
        this.data = new StoreMessageData(messageId, conversationId, nhsNumber, messageType, attachmentMessageIds);
    }
}
