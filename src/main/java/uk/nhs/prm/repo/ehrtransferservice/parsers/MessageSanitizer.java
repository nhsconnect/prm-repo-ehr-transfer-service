package uk.nhs.prm.repo.ehrtransferservice.parsers;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MessageSanitizer {
    public String sanitize(byte[] rawMessage) {
        String fullContent = new String(rawMessage, StandardCharsets.UTF_8);
        int startOfMessage = fullContent.indexOf("{\"ebXML\":");

        if (startOfMessage == -1) {
            return fullContent;
        }

        return fullContent.substring(startOfMessage);
    }
}
