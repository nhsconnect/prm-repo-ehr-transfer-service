package uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpmessagemodels;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.SOAPEnvelope;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.SOAPHeader;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.MessageHeader;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.SOAPBody;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.Reference;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.MessageData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Tag("unit")
public class ParsedMessageTest {
    private final Reference mid;
    private final Reference othermid;
    private final Reference cid;
    private final Reference invalid;
    private final String action;
    private final UUID conversationId;
    private final UUID messageId;

    public ParsedMessageTest() {
        mid = new Reference();
        mid.href = "mid:BFA900F3-4D4E-4661-8A78-82BE5742F0CB";

        othermid = new Reference();
        othermid.href = "mid:7D52B137-36CE-4179-8375-40B42AFCCF81";

        cid = new Reference();
        cid.href = "cid:something-else";

        invalid = new Reference();
        invalid.href = "bogus";

        action = "RCMR_IN030000UK06";
        conversationId = UUID.randomUUID();
        messageId = UUID.randomUUID();
    }

    @Test
    public void shouldReturnActionWhenSOAPIsValid() {
        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.header = new SOAPHeader();
        envelope.header.messageHeader = new MessageHeader();
        envelope.header.messageHeader.action = action;

        ParsedMessage message = new ParsedMessage(envelope);
        assertThat(message.getAction(), equalTo(action));
    }

    @Test
    public void shouldReturnNullWhenSOAPDoesNotHaveAHeader() {
        SOAPEnvelope envelope = new SOAPEnvelope();

        ParsedMessage message = new ParsedMessage(envelope);
        assertThat(message.getAction(), equalTo(null));
    }

    @Test
    public void shouldReturnLargeMessageTrueWhenThereIsMID() {
        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.body = new SOAPBody();
        envelope.body.manifest = new ArrayList<>();
        envelope.body.manifest.add(mid);
        ParsedMessage message = new ParsedMessage(envelope);
        assertThat(message.isLargeMessage(), equalTo(true));
    }

    @Test
    public void shouldReturnLargeMessageFalseWhenThereIsNoMID() {
        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.body = new SOAPBody();
        envelope.body.manifest = new ArrayList<>();
        envelope.body.manifest.add(cid);
        ParsedMessage message = new ParsedMessage(envelope);
        assertThat(message.isLargeMessage(), equalTo(false));
    }

    @Test
    public void shouldReturnLargeMessageFalseWhenReferenceIsInvalid() {
        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.body = new SOAPBody();
        envelope.body.manifest = new ArrayList<>();
        envelope.body.manifest.add(invalid);
        ParsedMessage message = new ParsedMessage(envelope);
        assertThat(message.isLargeMessage(), equalTo(false));
    }

    @Test
    public void shouldReturnEmptyArrayWhenNoAttachments() {
        List<UUID> expectedListOfMIDs = new ArrayList<>();

        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.body = new SOAPBody();
        envelope.body.manifest = new ArrayList<>();
        envelope.body.manifest.add(cid);
        ParsedMessage message = new ParsedMessage(envelope);
        assertThat(message.getAttachmentMessageIds(), equalTo(expectedListOfMIDs));
    }

    @Test
    public void shouldReturnArrayOfAttachmentMessageIdsWhenMessageHasAttachments() {
        List<UUID> expectedListOfMIDs = new ArrayList<>();
        expectedListOfMIDs.add(UUID.fromString("BFA900F3-4D4E-4661-8A78-82BE5742F0CB"));
        expectedListOfMIDs.add(UUID.fromString("7D52B137-36CE-4179-8375-40B42AFCCF81"));

        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.body = new SOAPBody();
        envelope.body.manifest = new ArrayList<>();
        envelope.body.manifest.add(mid);
        envelope.body.manifest.add(othermid);
        ParsedMessage message = new ParsedMessage(envelope);
        assertThat(message.getAttachmentMessageIds(), equalTo(expectedListOfMIDs));
    }

    @Test
    public void shouldReturnConversationIdWhenSOAPIsValid() {
        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.header = new SOAPHeader();
        envelope.header.messageHeader = new MessageHeader();
        envelope.header.messageHeader.conversationId = conversationId;
        ParsedMessage message = new ParsedMessage(envelope);
        assertThat(message.getConversationId(), equalTo(conversationId));
    }

    @Test
    public void shouldReturnNullForConversationIdWhenSOAPDoesNotHaveAMessageHeader() {
        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.header = new SOAPHeader();
        ParsedMessage message = new ParsedMessage(envelope);
        assertThat(message.getConversationId(), equalTo(null));
    }

    @Test
    public void shouldReturnMessageIdWhenSOAPIsValid() {
        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.header = new SOAPHeader();
        envelope.header.messageHeader = new MessageHeader();
        envelope.header.messageHeader.messageData = new MessageData();
        envelope.header.messageHeader.messageData.messageId = messageId;
        ParsedMessage message = new ParsedMessage(envelope);
        assertThat(message.getMessageId(), equalTo(messageId));
    }

    @Test
    public void shouldReturnNullForMessageIdWhenSOAPDoesNotHaveAMessageHeader() {
        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.header = new SOAPHeader();
        ParsedMessage message = new ParsedMessage(envelope);
        assertThat(message.getMessageId(), equalTo(null));
    }
}
