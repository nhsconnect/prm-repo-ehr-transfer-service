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
            "tppSmallEhr.xml, tppSmallEhrSanitized.xml"
    })
    public void shouldExtractActionNameFromSanitizedMessage(String inputMessage, String expectedOutputMessage) throws IOException {
        byte[] message = loader.getDataAsBytes(inputMessage);
        String sanitizedMessage = messageSanitizer.sanitize(message);
        String expectedText = loader.getDataAsString(expectedOutputMessage);
        assertThat(sanitizedMessage, equalTo(expectedText));
    }

    @Test
    void shouldNotChangeAMessageWithoutABoundary() {
        String nonMultipartMessage = "Not a multipart message";
        assertThat(messageSanitizer.sanitize(nonMultipartMessage.getBytes(StandardCharsets.UTF_8)), equalTo(nonMultipartMessage));
    }

    @Test
    void shouldRemoveExtraCharactersFromEMISResponse() throws IOException {
        byte[] rawMessageFromQueue = loader.getDataAsBytes("RCMR_IN010000UK05.xml");
        String sanitizedMessage = loader.getDataAsString("RCMR_IN010000UK05Sanitized.xml");
        assertThat(messageSanitizer.sanitize(rawMessageFromQueue), equalTo(sanitizedMessage));
    }

    @Test
    void shouldRemoveExtraCharactersFromTPPResponse() throws IOException {
        byte[] rawMessageFromQueue = loader.getDataAsBytes("RCMR_IN030000UK06.xml");
        String sanitizedMessage = loader.getDataAsString("RCMR_IN030000UK06Sanitized.xml");
        assertThat(messageSanitizer.sanitize(rawMessageFromQueue), equalTo(sanitizedMessage));
    }

}