package uk.nhs.prm.repo.ehrtransferservice.parsers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class MessageSanitizer {
    public String sanitize(byte[] rawMessage) {
        String fullContent = new String(rawMessage, StandardCharsets.UTF_8);
        int startOfMessage = fullContent.indexOf("{\"ebXML\":");

        if (startOfMessage == -1) {
            log.info("No need to sanitize");
            return fullContent;
        }

        log.info("Sanitizing message at position: ", startOfMessage);
        return fullContent.substring(startOfMessage);
    }
}
