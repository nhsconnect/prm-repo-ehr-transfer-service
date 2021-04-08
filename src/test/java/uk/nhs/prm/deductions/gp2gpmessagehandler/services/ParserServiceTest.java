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
//            "RCMR_IN010000UK05Sanitized.xml, RCMR_IN010000UK05",
//            "RCMR_IN030000UK06Sanitized.xml, RCMR_IN030000UK06",
//            "PRPA_IN000202UK01Sanitized.xml, PRPA_IN000202UK01",
            "tppSmallEhrSanitized.xml, RCMR_IN030000UK06"
    })
    public void shouldExtractActionNameFromSanitizedMessage(String fileName, String expectedInteractionId) throws IOException, MessagingException {
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString, null);

        assertThat(parsedMessage.getAction(), equalTo(expectedInteractionId));
    }

    @ParameterizedTest
    @CsvSource({
            "ehrOneLargeMessageSanitized.xml, true",
            "RCMR_IN030000UK06Sanitized.xml, false"
    })
    public void shouldCheckIfMessageIsLarge(String fileName, boolean isLargeMessage) throws IOException, MessagingException {
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString, null);

        assertThat(parsedMessage.isLargeMessage(), equalTo(isLargeMessage));
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN010000UK05Sanitized.xml, DFF5321C-C6EA-468E-BBC2-B0E48000E071",
            "RCMR_IN030000UK06Sanitized.xml, 5A36471B-036B-48E1-BBB4-A89AEE0652E1",
            "PRPA_IN000202UK01Sanitized.xml, 3B71EB7E-5F87-426D-AE23-E0EAFEB60BD4"
    })
    public void shouldExtractConversationIdFromSanitizedMessage(String fileName, UUID expectedConversationId) throws IOException, MessagingException {
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString, null);

        assertThat(parsedMessage.getConversationId(), equalTo(expectedConversationId));
    }

    @ParameterizedTest
    @CsvSource({
            "RCMR_IN010000UK05Sanitized.xml, DFF5321C-C6EA-468E-BBC2-B0E48000E071",
            "RCMR_IN030000UK06Sanitized.xml, 31FA3430-6E88-11EA-9384-E83935108FD5",
            "PRPA_IN000202UK01Sanitized.xml, D9B0D972-79C5-4144-B7FD-FE61EEF33E5F"
    })
    public void shouldExtractMessageIdFromSanitizedMessage(String fileName, UUID expectedMessageId) throws IOException, MessagingException {
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString, null);

        assertThat(parsedMessage.getMessageId(), equalTo(expectedMessageId));
    }

    @Test
    public void shouldExtractNhsNumberFromEhrExtract() throws IOException, MessagingException {
        String fileName = "RCMR_IN030000UK06Sanitized.xml";
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString, null);

        assertThat(parsedMessage.getNhsNumber(), equalTo("9442964410"));
    }

    @Test
    public void shouldNotExtractNhsNumberFromEhrRequest() throws IOException, MessagingException {
        String fileName = "RCMR_IN010000UK05Sanitized.xml";
        String messageAsString = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(messageAsString, null);

        assertThat(parsedMessage.getNhsNumber(), equalTo(null));
    }
}
