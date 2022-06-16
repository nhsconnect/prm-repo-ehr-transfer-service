package uk.nhs.prm.repo.ehrtransferservice.models;

// plural?
public class LargeMessageFragments extends LargeSqsMessage {

    private static final String EMPTY_NHS_NUMBER_REQUIRED_FOR_STORE_MESSAGE_CALL = "";

    public LargeMessageFragments(LargeSqsMessage largeSqsMessage) {
        super(largeSqsMessage.getSoapEnvelope(), largeSqsMessage.getMessageContent(), largeSqsMessage.getRawMessage());
    }

    @Override
    public String getNhsNumber() {
        return EMPTY_NHS_NUMBER_REQUIRED_FOR_STORE_MESSAGE_CALL;
    }
}
