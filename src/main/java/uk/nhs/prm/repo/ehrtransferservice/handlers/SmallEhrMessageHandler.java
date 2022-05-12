package uk.nhs.prm.repo.ehrtransferservice.handlers;

import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;

public class SmallEhrMessageHandler implements MessageHandler {
    @Override
    public String getInteractionId() {
        return null;
    }

    @Override
    public void handleMessage(ParsedMessage parsedMessage) {
        // to be implemented
    }
}
