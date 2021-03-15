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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Tag("unit")
public class ParsedMessageTest {
    private final Reference mid;
    private final Reference cid;
    private final Reference invalid;
    private final String action;
    private final String conversationId;
    private final String messageId;

    public ParsedMessageTest() {
        mid = new Reference();
        mid.href = "mid:something";

        cid = new Reference();
        cid.href = "cid:something-else";

        invalid = new Reference();
        invalid.href = "bogus";

        action = "RCMR_IN030000UK06";
        conversationId = "FFE3AF9D-8A11-4606-859D-CDBF469984E6";
        messageId = "EE662CBC-A847-47CF-93E9-301D78C31845";
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
