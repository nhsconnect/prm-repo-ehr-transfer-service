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
public class ParserTest {

    private final Parser parser = new Parser();
    private final TestDataLoader rawLoader = new TestDataLoader();
    private final ReadableTestDataHandler readableReader = new ReadableTestDataHandler();

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN010000UK05, Sanitized",
            "RCMR_IN030000UK06, Sanitized",
            "PRPA_IN000202UK01, Sanitized"
    })
    public void shouldExtractActionNameFromSanitizedMessage(String interactionId, String variant) throws IOException {
        String messageAsString = readableReader.readMessage(interactionId, variant);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getInteractionId(), equalTo(interactionId));
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN030000UK06, WithMidSanitized, true",
            "RCMR_IN030000UK06, Sanitized, false"
    })
    public void shouldCheckIfMessageIsLarge(String interactionId, String variant, boolean isLargeMessage) throws IOException {
        String messageAsString = readableReader.readMessage(interactionId, variant);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.isLargeMessage(), equalTo(isLargeMessage));
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN010000UK05, Sanitized, 17a757f2-f4d2-444e-a246-9cb77bef7f22",
            "RCMR_IN030000UK06, Sanitized, ff27abc3-9730-40f7-ba82-382152e6b90a",
            "PRPA_IN000202UK01, Sanitized, 723c5f3a-1ab8-4515-a582-3e5cc600bf59"
    })
    public void shouldExtractConversationIdFromSanitizedMessage(String interactionId, String variant, UUID expectedConversationId) throws IOException {
        String messageAsString = readableReader.readMessage(interactionId, variant);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getConversationId(), equalTo(expectedConversationId));
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN010000UK05, Sanitized, C445C720-B0EB-4E36-AF8A-48CD1CA5DE4F",
            "RCMR_IN030000UK06, Sanitized, 1C66BB0E-811E-4956-8F9C-33424695B75F",
            "PRPA_IN000202UK01, Sanitized, 11F4D7DF-EB49-45A5-A310-59FFFCF98C2A"
    })
    public void shouldExtractMessageIdFromSanitizedMessage(String interactionId, String variant, UUID expectedMessageId) throws IOException {
        String messageAsString = readableReader.readMessage(interactionId, variant);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getMessageId(), equalTo(expectedMessageId));
    }

    @Test
    public void shouldExtractNhsNumberFromEhrExtract() throws IOException {
        String fileName = "RCMR_IN030000UK06Sanitized";
        String messageAsString = rawLoader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getNhsNumber(), equalTo("9692842304"));
    }

    @Test
    public void shouldExtractErrorMessageFromAcknowledgementFromTheReasonsWhichShouldOnlyExistIfItIsAFailure() throws IOException {
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

    @Test
    public void shouldParseMessageWithUnexpectedBackslashInNhsNumber() throws IOException {
        String messageAsString = readableReader.readMessage("RCMR_IN030000UK06", "SanitizedWithUnexpectedBackslash");
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getNhsNumber(), equalTo("9692\\842304"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "simpleTextMessage.txt",
            "RCMR_IN030000UK06WithoutInteractionId",
            "RCMR_IN030000UK06WithoutMessageHeader",
            "RCMR_IN030000UK06WithoutSoapHeader",
            "RCMR_IN030000UK06WithIncorrectInteractionId"
    })
    void shouldThrowJsonParseExceptionWhenCannotParseMessage(String fileName) throws IOException {
        String messageAsString = rawLoader.getDataAsString(fileName);

        Exception expected = assertThrows(JsonParseException.class, () ->
                parser.parse(messageAsString)
        );
        assertThat(expected, notNullValue());
    }
}
