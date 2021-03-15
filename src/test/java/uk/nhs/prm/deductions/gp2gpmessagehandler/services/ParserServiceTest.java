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
            "ehrRequestSoapEnvelopeSanitized.xml, RCMR_IN010000UK05",
            "ehrRequestRCMR_IN030000UK06InteractionIdSanitized.xml, RCMR_IN030000UK06",
            "ehrRequestPRPA_IN000202UK01InteractionIdSanitized.xml, PRPA_IN000202UK01"
    })
    public void shouldExtractActionNameFromSanitizedMessage(String fileName, String expectedInteractionId) throws IOException, MessagingException {
        String message = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(message);
        assertThat(parsedMessage.getAction(), equalTo(expectedInteractionId));
    }

    @ParameterizedTest
    @CsvSource({
            "ehrOneLargeMessageSanitized.xml, true",
            "ehrRequestRCMR_IN030000UK06InteractionIdSanitized.xml, false"
    })
    public void shouldCheckIfMessageIsLarge(String fileName, boolean isLargeMessage) throws IOException, MessagingException {
        String message = loader.getDataAsString(fileName);
        ParsedMessage parsedMessage = parser.parse(message);
        assertThat(parsedMessage.isLargeMessage(), equalTo(isLargeMessage));
    }
}
