package uk.nhs.prm.repo.ehrtransferservice.models;

public class LargeMessageFragments extends LargeSqsMessage {

    public LargeMessageFragments(LargeSqsMessage largeSqsMessage) {
        super(largeSqsMessage.getSoapEnvelope(), largeSqsMessage.getMessageContent(), largeSqsMessage.getRawMessage());
    }

    @Override
    public String getNhsNumber() {
        return "";
    }
}
