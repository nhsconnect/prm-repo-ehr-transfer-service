package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.utils.TestDataLoader;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

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
            "JSONMessages/RCMR_IN010000UK05Sanitized, RCMR_IN010000UK05",
            "JSONMessages/RCMR_IN030000UK06Sanitized, RCMR_IN030000UK06",
            "JSONMessages/PRPA_IN000202UK01Sanitized, PRPA_IN000202UK01"
    })
    public void shouldExtractActionNameFromSanitizedMessage(String fileName, String expectedInteractionId) throws IOException, MessagingException {
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString, null);

        assertThat(parsedMessage.getAction(), equalTo(expectedInteractionId));
    }

    @ParameterizedTest
    @CsvSource({
            "JSONMessages/RCMR_IN030000UK06WithMidSanitized, true",
            "JSONMessages/RCMR_IN030000UK06Sanitized, false"
    })
    public void shouldCheckIfMessageIsLarge(String fileName, boolean isLargeMessage) throws IOException, MessagingException {
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString, null);

        assertThat(parsedMessage.isLargeMessage(), equalTo(isLargeMessage));
    }

    @ParameterizedTest
    @CsvSource({
            "JSONMessages/RCMR_IN010000UK05Sanitized, 17a757f2-f4d2-444e-a246-9cb77bef7f22",
            "JSONMessages/RCMR_IN030000UK06Sanitized, ff27abc3-9730-40f7-ba82-382152e6b90a",
            "JSONMessages/PRPA_IN000202UK01Sanitized, 723c5f3a-1ab8-4515-a582-3e5cc600bf59"
    })
    public void shouldExtractConversationIdFromSanitizedMessage(String fileName, UUID expectedConversationId) throws IOException, MessagingException {
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString, null);

        assertThat(parsedMessage.getConversationId(), equalTo(expectedConversationId));
    }

    @ParameterizedTest
    @CsvSource({
            "JSONMessages/RCMR_IN010000UK05Sanitized, C445C720-B0EB-4E36-AF8A-48CD1CA5DE4F",
            "JSONMessages/RCMR_IN030000UK06Sanitized, 1C66BB0E-811E-4956-8F9C-33424695B75F",
            "JSONMessages/PRPA_IN000202UK01Sanitized, 11F4D7DF-EB49-45A5-A310-59FFFCF98C2A"
    })
    public void shouldExtractMessageIdFromSanitizedMessage(String fileName, UUID expectedMessageId) throws IOException, MessagingException {
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString, null);

        assertThat(parsedMessage.getMessageId(), equalTo(expectedMessageId));
    }

    @Test
    public void shouldExtractNhsNumberFromEhrExtract() throws IOException, MessagingException {
        String fileName = "JSONMessages/RCMR_IN030000UK06Sanitized";
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString, null);

        assertThat(parsedMessage.getNhsNumber(), equalTo("9692842304"));
    }
}
