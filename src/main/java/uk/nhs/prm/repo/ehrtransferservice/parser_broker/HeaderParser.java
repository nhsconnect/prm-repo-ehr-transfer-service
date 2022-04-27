package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

@Component
@Slf4j
public class HeaderParser {

    public String getCorrelationId(byte[] rawMessage) {
        String fullContent = new String(rawMessage, StandardCharsets.UTF_8);

        int startOfCorrelationId = fullContent.indexOf("correlation-id");
        int endOfCorrelationId = fullContent.indexOf("{\"ebXML\"");

        // check if there is a correlation id in the headers
        if (startOfCorrelationId == -1) {
            log.info("No correlation id found for tracing in mhs inbound message headers");
            return null;
        }

        var correlationIdSubstring = fullContent.substring(startOfCorrelationId, endOfCorrelationId);

        // remove all dodgy characters from the substring
        Pattern unexpectedCharacters = compile("([^\\w\\-]+)");
        var correlationIdWithoutWeirdSymbols = unexpectedCharacters.matcher(correlationIdSubstring).replaceAll("");
        var removedCorrelationIdText = correlationIdWithoutWeirdSymbols.replace("correlation-id", "");

        // ensure we are left with only the uuid by getting substring length 36 (32 in uuid, plus 4 dashes)
        return removedCorrelationIdText.substring(0, 36);
    }
}
