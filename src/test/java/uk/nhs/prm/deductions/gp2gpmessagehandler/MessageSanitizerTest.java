package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.nhs.prm.deductions.gp2gpmessagehandler.utils.TestDataLoader;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@Tag("unit")
class MessageSanitizerTest {

    private TestDataLoader dataLoader = new TestDataLoader();

    MessageSanitizer messageSanitizer = new MessageSanitizer();
    @Test
    void shouldNotChangeAMessageWithoutABoundary() {
        String nonMultipartMessage = "Not a multipart message";
        assertThat(messageSanitizer.sanitize(nonMultipartMessage), equalTo(nonMultipartMessage));
    }

    @Test
    void shouldRemoveExtraCharactersBeforeFirstBoundaryWhenInputIsString() throws IOException {
        String rawMessageFromQueue = dataLoader.getDataAsString("RCMR_IN010000UK05.xml");
        String sanitizedMessage = dataLoader.getDataAsString("RCMR_IN010000UK05Sanitized.xml");
        assertThat(messageSanitizer.sanitize(rawMessageFromQueue), equalTo(sanitizedMessage));
    }

    @Test
    void shouldRemoveExtraCharactersBeforeFirstBoundaryWhenInputIsBytes() throws IOException {
        byte[] rawMessageFromQueue = dataLoader.getDataAsBytes("RCMR_IN010000UK05.xml");
        String sanitizedMessage = dataLoader.getDataAsString("RCMR_IN010000UK05Sanitized.xml");
        assertThat(messageSanitizer.sanitize(rawMessageFromQueue), equalTo(sanitizedMessage));
    }

}