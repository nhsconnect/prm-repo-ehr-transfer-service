package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.FailureLevel;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.Acknowlegement;
import uk.nhs.prm.repo.ehrtransferservice.utils.ReadableTestDataHandler;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import java.io.IOException;

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
    public void shouldExtractReasonStringsFromTheReasonsFor_AE_TypeFailure() throws IOException {
        String messageAsString = readableReader.readMessage("MCCI_IN010000UK13", "AE_TypeFailure");
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getReasons().size(), equalTo(2));
        assertThat(parsedMessage.getReasons().get(1), equalTo("Update Failed - invalid GP Registration data supplied"));
    }

    @Test
    public void shouldExtractFailureDetailsFromTheReasonsFor_AE_TypeFailure() throws IOException {
        String messageAsString = readableReader.readMessage("MCCI_IN010000UK13", "AE_TypeFailure");
        var parsedMessage = (Acknowlegement) parser.parse(messageAsString);

        assertThat(parsedMessage.getFailureDetails().size(), equalTo(2));
        var firstReasonFailure = parsedMessage.getFailureDetails().get(0);
        assertThat(firstReasonFailure.displayName(), equalTo("Update Failed"));
        assertThat(firstReasonFailure.code(), equalTo("15"));
        assertThat(firstReasonFailure.codeSystem(), equalTo("2.16.840.1.113883.2.1.3.2.4.17.42"));
        assertThat(firstReasonFailure.level(), equalTo(FailureLevel.WARNING));

        var secondReasonFailure = parsedMessage.getFailureDetails().get(1);
        assertThat(secondReasonFailure.displayName(), equalTo("Update Failed - invalid GP Registration data supplied"));
        assertThat(secondReasonFailure.code(), equalTo("IU058"));
        assertThat(secondReasonFailure.level(), equalTo(FailureLevel.ERROR));
    }

    @Test
    public void shouldExtractFailureDetailsFromTheReasonsFor_AR_TypeFailure() throws IOException {
        String messageAsString = readableReader.readMessage("MCCI_IN010000UK13", "AR_TypeFailure");
        var parsedMessage = (Acknowlegement) parser.parse(messageAsString);

        assertThat(parsedMessage.getFailureDetails().size(), equalTo(2));
        var firstReasonFailure = parsedMessage.getFailureDetails().get(0);
        assertThat(firstReasonFailure.displayName(), equalTo("Access denied"));
        assertThat(firstReasonFailure.code(), equalTo("1"));
        assertThat(firstReasonFailure.codeSystem(), equalTo("2.16.840.1.113883.2.1.3.2.4.17.32"));
        assertThat(firstReasonFailure.level(), equalTo(FailureLevel.ERROR));

        var secondReasonFailure = parsedMessage.getFailureDetails().get(1);
        assertThat(secondReasonFailure.displayName(), equalTo("Made up warning for testing"));
        assertThat(secondReasonFailure.code(), equalTo("2"));
        assertThat(secondReasonFailure.level(), equalTo(FailureLevel.WARNING));
    }

    @Test
    public void shouldNotFailWhenFailedToExtractErrorMessageFromNegativeAcknowledgement() throws IOException {
        String messageAsString = readableReader.readMessage("MCCI_IN010000UK13", "EmptyFailure");
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getReasons().size(), is(0));
    }
}
