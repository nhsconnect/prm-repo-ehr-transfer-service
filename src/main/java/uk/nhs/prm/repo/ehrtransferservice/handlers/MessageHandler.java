package uk.nhs.prm.repo.ehrtransferservice.handlers;

import uk.nhs.prm.repo.ehrtransferservice.gp2gpmessagemodels.ParsedMessage;

public interface MessageHandler {
    String getInteractionId();

    void handleMessage(ParsedMessage parsedMessage);
}
