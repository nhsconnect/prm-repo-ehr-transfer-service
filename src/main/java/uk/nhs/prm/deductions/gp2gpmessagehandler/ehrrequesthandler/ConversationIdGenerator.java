package uk.nhs.prm.deductions.gp2gpmessagehandler.ehrrequesthandler;

import org.springframework.stereotype.Component;

@Component
public class ConversationIdGenerator {
    String conversationId;

    public ConversationIdGenerator() {
    }
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
