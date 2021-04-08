package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

public interface MessageHandler {
    String getInteractionId();

    void handleMessage(ParsedMessage parsedMessage);
}
