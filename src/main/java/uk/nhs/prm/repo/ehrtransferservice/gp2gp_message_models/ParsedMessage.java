package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import org.apache.activemq.command.ActiveMQBytesMessage;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParsedMessage {
    private SOAPEnvelope soapEnvelope;
    private MessageContent messageContent;
    private String messageBody;

    public ParsedMessage(SOAPEnvelope soapEnvelope, MessageContent messageContent, String messageBody) {
        this.soapEnvelope = soapEnvelope;
        this.messageContent = messageContent;
        this.messageBody = messageBody;
    }

    public BytesMessage getBytesMessage() throws JMSException {
        final byte[] bytesArray = this.messageBody.getBytes(StandardCharsets.UTF_8);
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        bytesMessage.writeBytes(bytesArray);
        bytesMessage.reset();
        return bytesMessage;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public MessageContent getMessageContent() {
        return messageContent;
    }

    public SOAPEnvelope getSoapEnvelope() {
        return soapEnvelope;
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

    public String getInteractionId() {
        SOAPEnvelope soapEnvelope = getSoapEnvelope();
        if (soapEnvelope.header == null || soapEnvelope.header.messageHeader == null) {
            return null;
        }
        return soapEnvelope.header.messageHeader.action;
    }

    public UUID getConversationId() {
        SOAPEnvelope soapEnvelope = getSoapEnvelope();
        if (soapEnvelope.header == null || soapEnvelope.header.messageHeader == null) {
            return null;
        }
        return soapEnvelope.header.messageHeader.conversationId;
    }

    public UUID getMessageId() {
        SOAPEnvelope soapEnvelope = getSoapEnvelope();
        if (soapEnvelope.header == null || soapEnvelope.header.messageHeader == null || soapEnvelope.header.messageHeader.messageData == null) {
            return null;
        }
        return soapEnvelope.header.messageHeader.messageData.messageId;
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
}
