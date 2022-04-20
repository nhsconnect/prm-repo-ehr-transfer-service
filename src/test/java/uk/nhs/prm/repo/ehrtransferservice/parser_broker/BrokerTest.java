package uk.nhs.prm.repo.ehrtransferservice.parser_broker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.AttachmentMessagePublisher;
import uk.nhs.prm.repo.ehrtransferservice.message_publishers.SmallEhrMessagePublisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BrokerTest {
    @Mock
    AttachmentMessagePublisher attachmentMessagePublisher;
    @Mock
    SmallEhrMessagePublisher smallEhrMessagePublisher;

    @InjectMocks
    Broker broker;

    @Test
    public void shouldSendCopcMessageToAttachmentMessagePublisher()  {
        var parsedMessage = new ParsedMessage(new SOAPEnvelope(), null, "copc-raw-message");
        broker.sendMessageToCorrespondingTopicPublisher("COPC_IN000001UK01", parsedMessage);

        verify(attachmentMessagePublisher).sendMessage(eq("copc-raw-message"), any());
    }

    @Test
    public void shouldSendSmallEhrMessageToSmallEhrMessagePublisher()  {
        var parsedMessage = new ParsedMessage(new SOAPEnvelope(), null, "ehr-raw-message");
        broker.sendMessageToCorrespondingTopicPublisher("RCMR_IN030000UK06", parsedMessage);

        verify(smallEhrMessagePublisher).sendMessage(eq("ehr-raw-message"), any());
    }
}
