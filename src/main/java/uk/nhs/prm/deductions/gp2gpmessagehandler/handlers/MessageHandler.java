package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;

import javax.jms.BytesMessage;

public interface MessageHandler {
    String getInteractionId();

    void handleMessage(ParsedMessage parsedMessage, BytesMessage bytesMessage);
}