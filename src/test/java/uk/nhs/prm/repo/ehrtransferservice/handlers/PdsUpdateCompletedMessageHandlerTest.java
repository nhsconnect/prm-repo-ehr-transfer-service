package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.repo.ehrtransferservice.JmsProducer;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MessageHeader;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPEnvelope;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.SOAPHeader;
import uk.nhs.prm.repo.ehrtransferservice.services.HttpException;
import uk.nhs.prm.repo.ehrtransferservice.services.gp_to_repo.GPToRepoClient;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Tag("unit")
public class PdsUpdateCompletedMessageHandlerTest {
    @Mock
    JmsProducer jmsProducer;

    @Mock
    GPToRepoClient gpToRepoClient;

    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;
    @InjectMocks
    PdsUpdateCompletedMessageHandler pdsUpdateCompletedMessageHandler;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    public ParsedMessage createParsedMessage() {
        SOAPEnvelope soapEnvelope = new SOAPEnvelope();
        soapEnvelope.header = new SOAPHeader();
        soapEnvelope.header.messageHeader = new MessageHeader();
        soapEnvelope.header.messageHeader.conversationId = UUID.fromString("614DB042-A2B0-4A2A-A0DC-E12C62E13C9F");
        return new ParsedMessage(soapEnvelope, null, null);
    }

    @Test
    public void shouldReturnCorrectInteractionId() {
        assertThat(pdsUpdateCompletedMessageHandler.getInteractionId(), equalTo("PRPA_IN000202UK01"));
    }

    @Test
    public void shouldCallGpToRepoWithTheConversationId() throws URISyntaxException, HttpException, MalformedURLException {
        ParsedMessage parsedMessage = createParsedMessage();
        pdsUpdateCompletedMessageHandler.handleMessage(parsedMessage);
        verify(gpToRepoClient, times(1)).sendPdsUpdatedMessage(parsedMessage.getSoapEnvelope().header.messageHeader.conversationId);
    }

    @Test
    public void shouldSendToUnhandledQueueAndThrowWhenGpToRepoClientFails() throws URISyntaxException, HttpException, MalformedURLException {
        ParsedMessage parsedMessage = createParsedMessage();
        doThrow(new HttpException("could not connect in test")).when(gpToRepoClient).sendPdsUpdatedMessage(any());
        assertThrows(RuntimeException.class, () -> pdsUpdateCompletedMessageHandler.handleMessage(parsedMessage));
        verify(jmsProducer, times(1)).sendMessageToQueue(unhandledQueue, parsedMessage.getRawMessage());
    }
}
