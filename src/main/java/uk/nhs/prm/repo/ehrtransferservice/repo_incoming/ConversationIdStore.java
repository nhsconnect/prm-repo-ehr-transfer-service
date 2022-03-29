package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import org.springframework.stereotype.Component;

@Component
public class ConversationIdStore {
    String conversationId;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
