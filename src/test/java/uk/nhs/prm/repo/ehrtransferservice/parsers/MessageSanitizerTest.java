package uk.nhs.prm.repo.ehrtransferservice.parsers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Tag("unit")
class MessageSanitizerTest {

    MessageSanitizer messageSanitizer = new MessageSanitizer();
    private final TestDataLoader loader = new TestDataLoader();

    @ParameterizedTest
    @CsvSource({
            "PRPA_IN000202UK01, PRPA_IN000202UK01Sanitized",
            "RCMR_IN030000UK06, RCMR_IN030000UK06Sanitized",
            "RCMR_IN010000UK05, RCMR_IN010000UK05Sanitized",
            "COPC_IN000001UK01, COPC_IN000001UK01Sanitized",
    })
    void shouldRemovePrefixCharactersFromJsonMessage(String rawMessageFile, String sanitizedMessageFile) throws IOException {
        byte[] rawMessageFromQueue = loader.getDataAsBytes(rawMessageFile);
        String sanitizedMessage = loader.getDataAsString(sanitizedMessageFile);
        Assertions.assertEquals(messageSanitizer.sanitize(rawMessageFromQueue), sanitizedMessage);
    }

    @Test
    void shouldNotChangeAMessageWithoutExpectedEbxml() {
        String nonEbxmlMessage = "Not an ebxml message";
        Assertions.assertEquals(messageSanitizer.sanitize(nonEbxmlMessage.getBytes(StandardCharsets.UTF_8)), nonEbxmlMessage);
    }
}