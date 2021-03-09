package uk.nhs.prm.deductions.gp2gpmessagehandler;

import javax.mail.util.ByteArrayDataSource;
import java.nio.charset.StandardCharsets;

public class MessageSanitizer {
    public String sanitize(String rawMessageFromQueue) {
        int startOfMessage = rawMessageFromQueue.indexOf("--");
        if (startOfMessage == -1) {
            return rawMessageFromQueue;
        }
        return rawMessageFromQueue.substring(startOfMessage);
    }

    public String sanitize(byte[] rawMessage) {
        String fullContent = new String(rawMessage, StandardCharsets.UTF_8);
        return this.sanitize(fullContent);
    }
}
