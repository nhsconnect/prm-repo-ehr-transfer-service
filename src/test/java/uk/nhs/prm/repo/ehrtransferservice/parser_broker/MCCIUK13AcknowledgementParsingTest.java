package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.utils.ReadableTestDataHandler;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
public class MCCIUK13AcknowledgementParsingTest {

    private final Parser parser = new Parser();
    private final TestDataLoader rawLoader = new TestDataLoader();
    private final ReadableTestDataHandler readableReader = new ReadableTestDataHandler();

    @Test
    public void shouldExtractErrorsFromTheReasonsWhichShouldOnlyExistIfItIsAnAETypeFailure() throws IOException {
        String messageAsString = readableReader.readMessage("MCCI_IN010000UK13", "FailureSanitized");
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        var rawMessage = parsedMessage.getRawMessage();

        System.out.println(rawMessage);

        assertThat(parsedMessage.getReasons().size(), equalTo(2));
        assertThat(parsedMessage.getReasons().get(1), equalTo("Update Failed - invalid GP Registration data supplied"));
    }

    @Test
    public void shouldNotFailWhenFailedToExtractMessageFromAcknowledgement() throws IOException {
        String messageAsString = readableReader.readMessage("MCCI_IN010000UK13", "Empty");
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getReasons().size(), is(0));
    }
}
