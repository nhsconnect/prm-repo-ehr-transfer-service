package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MessageSanitizer {
    public String sanitize(byte[] rawMessage) {
        String fullContent = new String(rawMessage, StandardCharsets.UTF_8);
        String firstLine = fullContent.split("\n")[0];
        int startOfMessage = firstLine.lastIndexOf("--");

        if (firstLine.endsWith("----=_MIME-Boundary")) {
            startOfMessage = firstLine.lastIndexOf("----=_MIME-Boundary");
        }
        // This is true for TPP messages
        else if (firstLine.endsWith("----=_MIME-Boundary\r")) {
            startOfMessage = firstLine.lastIndexOf("----=_MIME-Boundary\r");
        }
        if (startOfMessage == -1) {
            return fullContent;
        }
        return fullContent.substring(startOfMessage);
    }

    public String sanitizeNew(byte[] rawMessage) {
        String fullContent = new String(rawMessage, StandardCharsets.UTF_8);
        int startOfMessage = fullContent.indexOf("{\"ebXML\":");

        if (startOfMessage == -1) {
            return fullContent;
        }

        return fullContent.substring(startOfMessage);
    }
}
