package uk.nhs.prm.repo.ehrtransferservice.models;

import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;

public class LargeEhrMessageFragment extends LargeSqsMessage {

    private static final String EMPTY_NHS_NUMBER_REQUIRED_FOR_STORE_MESSAGE_CALL = "";

    public LargeEhrMessageFragment(ParsedMessage largeSqsMessage) {
        super(largeSqsMessage.getSoapEnvelope(), largeSqsMessage.getMessageContent(), largeSqsMessage.getRawMessage());
    }

    @Override
    public String getNhsNumber() {
        return EMPTY_NHS_NUMBER_REQUIRED_FOR_STORE_MESSAGE_CALL;
    }
}
