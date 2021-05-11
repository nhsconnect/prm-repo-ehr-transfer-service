package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.nhs.prm.deductions.gp2gpmessagehandler.utils.TestDataLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@Tag("unit")
class MessageSanitizerTest {

    private TestDataLoader loader = new TestDataLoader();

    MessageSanitizer messageSanitizer = new MessageSanitizer();

    @ParameterizedTest
    @CsvSource({
            "JSONMessages/PRPA_IN000202UK01, JSONMessages/PRPA_IN000202UK01Sanitized",
            "JSONMessages/RCMR_IN030000UK06, JSONMessages/RCMR_IN030000UK06Sanitized",
            "JSONMessages/RCMR_IN010000UK05, JSONMessages/RCMR_IN010000UK05Sanitized",
            "JSONMessages/COPC_IN000001UK01, JSONMessages/COPC_IN000001UK01Sanitized",
    })
    void shouldRemovePrefixCharactersFromJsonMessage(String rawMessageFile, String sanitizedMessageFile) throws IOException {
        byte[] rawMessageFromQueue = loader.getDataAsBytes(rawMessageFile);
        String sanitizedMessage = loader.getDataAsString(sanitizedMessageFile);
        assertThat(messageSanitizer.sanitize(rawMessageFromQueue), equalTo(sanitizedMessage));
    }

    @Test
    void shouldNotChangeAMessageWithoutExpectedEbxml() {
        String nonEbxmlMessage = "Not an ebxml message";
        assertThat(messageSanitizer.sanitize(nonEbxmlMessage.getBytes(StandardCharsets.UTF_8)), equalTo(nonEbxmlMessage));
    }

}