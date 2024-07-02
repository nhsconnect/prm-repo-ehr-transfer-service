package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import lombok.Getter;

@Getter
public class Gp2gpMessengerNegativeAcknowledgementRequestBody extends Gp2gpMessengerAcknowledgementRequestBody {
    private final String errorCode;

    public Gp2gpMessengerNegativeAcknowledgementRequestBody(
            String repositoryAsid,
            String odsCode,
            String conversationId,
            String messageId,
            String errorCode
    ) {
        super(repositoryAsid, odsCode, conversationId, messageId);
        this.errorCode = errorCode;
    }
}
