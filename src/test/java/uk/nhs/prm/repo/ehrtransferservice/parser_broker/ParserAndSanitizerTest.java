package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.nhs.prm.repo.ehrtransferservice.MessageSanitizer;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@Tag("unit")
public class ParserAndSanitizerTest {
    private final Parser parser;
    private final TestDataLoader loader;
    private final MessageSanitizer sanitizer;

    public ParserAndSanitizerTest() {
        sanitizer = new MessageSanitizer();
        parser = new Parser();
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
