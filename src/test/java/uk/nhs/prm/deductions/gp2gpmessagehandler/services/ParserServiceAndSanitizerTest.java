package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.nhs.prm.deductions.gp2gpmessagehandler.MessageSanitizer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.utils.TestDataLoader;

import javax.mail.MessagingException;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@Tag("unit")
public class ParserServiceAndSanitizerTest {
    private final ParserService parser;
    private final TestDataLoader loader;
    private final MessageSanitizer sanitizer;

    public ParserServiceAndSanitizerTest() {
        sanitizer = new MessageSanitizer();
        parser = new ParserService();
        loader = new TestDataLoader();
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN030000UK06, RCMR_IN030000UK06"
    })
    public void shouldExtractActionNameFromSanitizedMessage(String fileName, String expectedInteractionId) throws IOException {
        byte[] message = loader.getDataAsBytes(fileName);
        String sanitizedMessage = sanitizer.sanitize(message);

        ParsedMessage parsedMessage = parser.parse(sanitizedMessage);
        assertThat(parsedMessage.getAction(), equalTo(expectedInteractionId));
    }
}
