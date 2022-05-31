package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Gp2gpMessengerPositiveAcknowledgementRequestBody {
    private final String repositoryAsid;
    private final String previousOdsCode;
    private final String conversationId;
    private final String messageId;

    public Gp2gpMessengerPositiveAcknowledgementRequestBody(@JsonProperty("repositoryAsid") String repositoryAsid,
                                                            @JsonProperty("previousOdsCode") String previousOdsCode,
                                                            @JsonProperty("conversationId") String conversationId,
                                                            @JsonProperty("messageId") String messageId
    ) {
        this.repositoryAsid = repositoryAsid;
        this.previousOdsCode = previousOdsCode;
        this.conversationId = conversationId;
        this.messageId = messageId;
    }
}
