package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParsedMessage {
    private SOAPEnvelope soapEnvelope;

    public ParsedMessage(SOAPEnvelope soapEnvelope) {
        this.soapEnvelope = soapEnvelope;
    }

    public String getAction() {
        if (soapEnvelope.header == null || soapEnvelope.header.messageHeader == null) {
            return null;
        }
        return soapEnvelope.header.messageHeader.action;
    }

    public UUID getConversationId() {
        if (soapEnvelope.header == null || soapEnvelope.header.messageHeader == null) {
            return null;
        }
        return soapEnvelope.header.messageHeader.conversationId;
    }

    public UUID getMessageId() {
        if (soapEnvelope.header == null || soapEnvelope.header.messageHeader == null || soapEnvelope.header.messageHeader.messageData == null) {
            return null;
        }
        return soapEnvelope.header.messageHeader.messageData.messageId;
    }

    public boolean isLargeMessage() {
        for (Reference reference: soapEnvelope.body.manifest) {
            if (reference.href.contains("mid")) {
                return true;
            }
        }
        return false;
    }

    public List<UUID> getAttachmentMessageIds(){
        List<UUID> attachmentMessageIds = new ArrayList<>();

        for (Reference reference : soapEnvelope.body.manifest) {
            if (reference.href.contains("mid")) {
                String attachmentMessageId = reference.href.substring(4);
                attachmentMessageIds.add(UUID.fromString(attachmentMessageId));
            }
        }
        return attachmentMessageIds;
    }
}
