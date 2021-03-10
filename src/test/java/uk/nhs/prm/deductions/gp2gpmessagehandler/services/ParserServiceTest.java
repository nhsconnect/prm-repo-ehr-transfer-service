package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

import org.junit.jupiter.api.Test;
import uk.nhs.prm.deductions.gp2gpmessagehandler.MessageSanitizer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.utils.TestDataLoader;

import javax.mail.MessagingException;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ParserServiceTest {

    private final ParserService parser;
    private final TestDataLoader loader;

    public ParserServiceTest() {
        parser = new ParserService();
        loader = new TestDataLoader();
    }

    @Test // could be param. test
    public void shouldExtractActionNameFromMessage() throws IOException, MessagingException {
        String message = loader.getDataAsString("ehrRequestSoapEnvelopeSanitized.xml");
        ParsedMessage parsedMessage = parser.parse(message);
        assertThat(parsedMessage.getAction(), equalTo("RCMR_IN010000UK05"));
    }

    //TODO: move other test cases from JMS consumer
}
