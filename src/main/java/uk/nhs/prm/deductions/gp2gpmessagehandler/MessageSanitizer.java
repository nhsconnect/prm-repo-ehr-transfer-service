package uk.nhs.prm.deductions.gp2gpmessagehandler;

public class MessageSanitizer {
    public String sanitize(String rawMessageFromQueue) {
        int startOfMessage = rawMessageFromQueue.indexOf("--");
        if (startOfMessage == -1) {
            return rawMessageFromQueue;
        }
        return rawMessageFromQueue.substring(startOfMessage);
    }
}
