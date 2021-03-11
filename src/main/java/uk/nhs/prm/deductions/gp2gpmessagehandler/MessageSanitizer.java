package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
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
