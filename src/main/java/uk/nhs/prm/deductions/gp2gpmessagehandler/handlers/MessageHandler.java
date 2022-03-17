package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpmessagemodels.ParsedMessage;

public interface MessageHandler {
    String getInteractionId();

    void handleMessage(ParsedMessage parsedMessage);
}
