package uk.nhs.prm.deductions.gp2gpmessagehandler.ehrrequesthandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class RepoIncomingEventParserTest {

    @Test
    void shouldParseRepoIncomingMessageCorrectlyWhenAMessageContainsExpectedValues() throws JsonProcessingException {
        String incomingMessage = "{\"nhsNumber\":\"nhs-number\",\"sourceGP\":\"source-gp\",\"nemsMessageId\":\"nems-message-id\",\"destinationGP\":\"destination-GP\"}";
        var repoIncomingEventParser = new RepoIncomingEventParser();
        var parsedMessage = repoIncomingEventParser.parse(incomingMessage);
        assertEquals("nhs-number",parsedMessage.nhsNumber());
    }

    @Test
    void shouldThrowAnExceptionWhenItTriesToParseAGarbageMessage(){
        String incomingMessage = "invalid";
        var repoIncomingEventParser = new RepoIncomingEventParser();
        assertThrows(JsonProcessingException.class,() -> repoIncomingEventParser.parse(incomingMessage));

    }

}