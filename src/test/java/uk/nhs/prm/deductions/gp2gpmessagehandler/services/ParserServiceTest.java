package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.utils.TestDataLoader;

import javax.mail.MessagingException;
import java.io.IOException;

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
            "RCMR_IN010000UK05Sanitized.xml, RCMR_IN010000UK05",
            "RCMR_IN030000UK06Sanitized.xml, RCMR_IN030000UK06",
            "PRPA_IN000202UK01Sanitized.xml, PRPA_IN000202UK01"
    })
    public void shouldExtractActionNameFromSanitizedMessage(String fileName, String expectedInteractionId) throws IOException, MessagingException {
        String message = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(message);
        assertThat(parsedMessage.getAction(), equalTo(expectedInteractionId));
    }

    @ParameterizedTest
    @CsvSource({
            "ehrOneLargeMessageSanitized.xml, true",
            "RCMR_IN030000UK06Sanitized.xml, false"
    })
    public void shouldCheckIfMessageIsLarge(String fileName, boolean isLargeMessage) throws IOException, MessagingException {
        String message = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(message);
        assertThat(parsedMessage.isLargeMessage(), equalTo(isLargeMessage));
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN010000UK05Sanitized.xml, DFF5321C-C6EA-468E-BBC2-B0E48000E071",
            "RCMR_IN030000UK06Sanitized.xml, 5A36471B-036B-48E1-BBB4-A89AEE0652E1",
            "PRPA_IN000202UK01Sanitized.xml, 761f1012-396c-43d2-9684-d87354ac8e50"
    })
    public void shouldExtractConversationIdFromSanitizedMessage(String fileName, String expectedConversationId) throws IOException, MessagingException {
        String message = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(message);
        assertThat(parsedMessage.getConversationId(), equalTo(expectedConversationId));
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN010000UK05Sanitized.xml, DFF5321C-C6EA-468E-BBC2-B0E48000E071",
            "RCMR_IN030000UK06Sanitized.xml, 31FA3430-6E88-11EA-9384-E83935108FD5",
            "PRPA_IN000202UK01Sanitized.xml, D2B59ACB-68AF-462C-B0CC-387A1201F667"
    })
    public void shouldExtractMessageIdFromSanitizedMessage(String fileName, String expectedMessageId) throws IOException, MessagingException {
        String message = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(message);
        assertThat(parsedMessage.getMessageId(), equalTo(expectedMessageId));
    }
}
