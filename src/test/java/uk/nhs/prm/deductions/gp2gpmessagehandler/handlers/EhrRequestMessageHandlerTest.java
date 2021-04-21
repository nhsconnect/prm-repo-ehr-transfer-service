package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.HttpException;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.RepoToGPClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@Tag("unit")
public class EhrRequestMessageHandlerTest {
    @Mock
    JmsProducer jmsProducer;

    @Mock
    ParsedMessage parsedMessage;

    @Mock
    RepoToGPClient repoToGPClient;

    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;

    private AutoCloseable closeable;
    private UUID conversationId;
    private String ehrRequestMessageId;
    private String nhsNumber = "1234567890";
    private String odsCode = "A12345";

    @InjectMocks
    EhrRequestMessageHandler ehrRequestMessageHandler;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        conversationId = UUID.randomUUID();
        ehrRequestMessageId = UUID.randomUUID().toString();

        when(parsedMessage.getConversationId()).thenReturn(conversationId);
        when(parsedMessage.getEhrRequestId()).thenReturn(ehrRequestMessageId);
        when(parsedMessage.getNhsNumber()).thenReturn(nhsNumber);
        when(parsedMessage.getOdsCode()).thenReturn(odsCode);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void shouldReturnCorrectInteractionId() {
        assertThat(ehrRequestMessageHandler.getInteractionId(), equalTo("RCMR_IN010000UK05"));
    }

    @Test
    public void shouldCallRepoToGPToSendEhrRequest() throws HttpException, URISyntaxException, InterruptedException, IOException {
        ehrRequestMessageHandler.handleMessage(parsedMessage);
        verify(repoToGPClient).sendEhrRequest(ehrRequestMessageId, conversationId, nhsNumber, odsCode);
    }

    @Test
    public void shouldPutMessageOnUnhandledQueueWhenRepoToGPCallThrows() throws HttpException, InterruptedException, IOException, URISyntaxException {
        String message = "test";
        when(parsedMessage.getRawMessage()).thenReturn(message);
        // do we want a runtime or HTTP exception?
        RuntimeException expectedError = new RuntimeException("Failed to send deduction request");
        doThrow(expectedError).when(repoToGPClient).sendEhrRequest(ehrRequestMessageId, conversationId, nhsNumber, odsCode);

        ehrRequestMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, times(1)).sendMessageToQueue(unhandledQueue, message);
    }
}