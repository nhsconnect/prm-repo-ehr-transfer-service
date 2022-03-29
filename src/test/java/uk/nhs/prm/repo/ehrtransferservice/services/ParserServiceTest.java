package uk.nhs.prm.repo.ehrtransferservice.services;

import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.utils.TestDataLoader;

import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
public class ParserServiceTest {

    private final ParserService parser;
    private final TestDataLoader loader;

    public ParserServiceTest() {
        parser = new ParserService();
        loader = new TestDataLoader();
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN010000UK05Sanitized, RCMR_IN010000UK05",
            "RCMR_IN030000UK06Sanitized, RCMR_IN030000UK06",
            "PRPA_IN000202UK01Sanitized, PRPA_IN000202UK01"
    })
    public void shouldExtractActionNameFromSanitizedMessage(String fileName, String expectedInteractionId) throws IOException {
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getAction(), equalTo(expectedInteractionId));
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN030000UK06WithMidSanitized, true",
            "RCMR_IN030000UK06Sanitized, false"
    })
    public void shouldCheckIfMessageIsLarge(String fileName, boolean isLargeMessage) throws IOException {
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.isLargeMessage(), equalTo(isLargeMessage));
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN010000UK05Sanitized, 17a757f2-f4d2-444e-a246-9cb77bef7f22",
            "RCMR_IN030000UK06Sanitized, ff27abc3-9730-40f7-ba82-382152e6b90a",
            "PRPA_IN000202UK01Sanitized, 723c5f3a-1ab8-4515-a582-3e5cc600bf59"
    })
    public void shouldExtractConversationIdFromSanitizedMessage(String fileName, UUID expectedConversationId) throws IOException {
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getConversationId(), equalTo(expectedConversationId));
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN010000UK05Sanitized, C445C720-B0EB-4E36-AF8A-48CD1CA5DE4F",
            "RCMR_IN030000UK06Sanitized, 1C66BB0E-811E-4956-8F9C-33424695B75F",
            "PRPA_IN000202UK01Sanitized, 11F4D7DF-EB49-45A5-A310-59FFFCF98C2A"
    })
    public void shouldExtractMessageIdFromSanitizedMessage(String fileName, UUID expectedMessageId) throws IOException {
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getMessageId(), equalTo(expectedMessageId));
    }

    @Test
    public void shouldExtractNhsNumberFromEhrExtract() throws IOException {
        String fileName = "RCMR_IN030000UK06Sanitized";
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getNhsNumber(), equalTo("9692842304"));
    }

    @Test
    public void shouldExtractErrorMessageFromAcknowledgement() throws IOException {
        String fileName = "MCCI_IN010000UK13FailureSanitized";
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getReasons().size(), equalTo(2));
        assertThat(parsedMessage.getReasons().get(1), equalTo("Update Failed - invalid GP Registration data supplied"));
    }

    @Test
    public void shouldNotFailWhenFailedToExtractMessageFromAcknowledgement() throws IOException {
        String fileName = "MCCI_IN010000UK13Empty";
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString);

        assertThat(parsedMessage.getReasons().size(), is(0));
    }

    @Test
    public void shouldParseMessageWithUnexpectedBackslashInNhsNumber() throws IOException {
        String fileName = "RCMR_IN030000UK06SanitizedWithUnexpectedBackslash";
        String messageAsString = loader.getDataAsString(fileName);
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
        String messageAsString = loader.getDataAsString(fileName);

        Exception expected = assertThrows(JsonParseException.class, () ->
                parser.parse(messageAsString)
        );
        assertThat(expected, notNullValue());
    }
}
