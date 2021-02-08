package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.junit.jupiter.api.Test;
import uk.nhs.prm.deductions.gp2gpmessagehandler.utils.TestDataLoader;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

class MessageSanitizerTest {

    private TestDataLoader dataLoader = new TestDataLoader();

    MessageSanitizer messageSanitizer = new MessageSanitizer();
    @Test
    void shouldNotChangeAMessageWithoutABoundary() {
        String nonMultipartMessage = "Not a multipart message";
        assertThat(messageSanitizer.sanitize(nonMultipartMessage), equalTo(nonMultipartMessage));
    }

    @Test
    void shouldRemoveExtraCharactersBeforeFirstBoundary() throws IOException {
        String rawMessageFromQueue = dataLoader.getData("ehrRequestSoapEnvelope.xml");
        String sanitizedMessage = dataLoader.getData("ehrRequestSoapEnvelopeSanitized.xml");
        assertThat(messageSanitizer.sanitize(rawMessageFromQueue), equalTo(sanitizedMessage));
    }

}