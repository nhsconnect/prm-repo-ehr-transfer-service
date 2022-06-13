package uk.nhs.prm.repo.ehrtransferservice;

import java.util.UUID;

public class Gp2gpNackBuilder {
    private UUID conversationId;
    private String errorCode;
    private String errorDisplayText;

    public Gp2gpNackBuilder withConversationId(UUID conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public Gp2gpNackBuilder withErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public Gp2gpNackBuilder withErrorDisplayText(String errorDisplayText) {
        this.errorDisplayText = errorDisplayText;
        return this;
    }


    public String build() {
        return "some ack";
    }
}
