package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParsedMessage extends BaseParsedMessage {
    private MessageContent messageContent;

    public ParsedMessage(SOAPEnvelope soapEnvelope, MessageContent messageContent, String rawMessage) {
        super(soapEnvelope, rawMessage);
        this.messageContent = messageContent;
    }

    public String getNhsNumber() {
        if (messageContent instanceof EhrExtractMessageWrapper) {
            return ((EhrExtractMessageWrapper) messageContent).getEhrExtract().getPatient().getNhsNumber();
        }

        if (messageContent instanceof EhrRequestMessageWrapper) {
            return ((EhrRequestMessageWrapper) messageContent).getEhrRequest().getPatient().getNhsNumber();
        }
        return null;
    }

    public String getEhrRequestId() {
        if (messageContent instanceof EhrRequestMessageWrapper) {
            return ((EhrRequestMessageWrapper) messageContent).getEhrRequest().getId();
        }
        return null;
    }

    public String getOdsCode() {
        if (messageContent instanceof EhrRequestMessageWrapper) {
            return ((EhrRequestMessageWrapper) messageContent).getEhrRequest().getRequestingPractice().getOdsCode();
        }
        return null;
    }

    public boolean isLargeMessage() {
        SOAPEnvelope soapEnvelope = getSoapEnvelope();

        for (Reference reference : soapEnvelope.body.manifest) {
            if (reference.href.contains("mid")) {
                return true;
            }
        }
        return false;
    }

    public List<UUID> getAttachmentMessageIds() {
        List<UUID> attachmentMessageIds = new ArrayList<>();
        SOAPEnvelope soapEnvelope = getSoapEnvelope();

        for (Reference reference : soapEnvelope.body.manifest) {
            if (reference.href.contains("mid")) {
                String attachmentMessageId = reference.href.substring(4);
                attachmentMessageIds.add(UUID.fromString(attachmentMessageId));
            }
        }
        return attachmentMessageIds;
    }

    public List<String> getReasons() {
        if (messageContent instanceof AcknowledgementMessageWrapper) {
            return ((AcknowledgementMessageWrapper) messageContent).getReasons();
        }
        return new ArrayList<>();
    }

    public boolean isNegativeAcknowledgement() {
        if (messageContent instanceof AcknowledgementMessageWrapper) {
            return getReasons() != null && getReasons().size() > 0;
        }
        return false;
    }
}
