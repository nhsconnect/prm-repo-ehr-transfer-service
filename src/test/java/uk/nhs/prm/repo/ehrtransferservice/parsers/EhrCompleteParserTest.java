package uk.nhs.prm.repo.ehrtransferservice.parsers;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EhrCompleteParserTest {

    @Test
    void shouldParseRepoIncomingMessageCorrectlyWhenAMessageContainsExpectedValues() {
        var conversationId = UUID.randomUUID();
        var messageId = UUID.randomUUID();
        String incomingMessage = "{\"conversationId\":\"" + conversationId + "\",\"messageId\":\"" + messageId + "\"}";

        var ehrCompleteParser = new EhrCompleteParser();
        var parsedMessage = ehrCompleteParser.parse(incomingMessage);

        assertEquals(conversationId, parsedMessage.getConversationId());
        assertEquals(messageId, parsedMessage.getMessageId());
    }

    @Test
    void shouldThrowExceptionWhenParsingIncorrectlyFormattedMessage() {
        String invalidIncomingMessage = "invalid";

        EhrCompleteParser ehrCompleteParser = new EhrCompleteParser();

        assertThrows(RuntimeException.class, () -> ehrCompleteParser.parse(invalidIncomingMessage));
    }

}