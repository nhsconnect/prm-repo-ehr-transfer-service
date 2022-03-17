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
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpmessagemodels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.EhrRepoService;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.HttpException;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@Tag("unit")
public class CopcMessageHandlerTest {
    @Mock
    JmsProducer jmsProducer;
    @Mock
    EhrRepoService ehrRepoService;

    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;

    private AutoCloseable closeable;

    @InjectMocks
    CopcMessageHandler copcMessageHandler;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void shouldReturnCorrectInteractionId() {
        assertThat(copcMessageHandler.getInteractionId(), equalTo("COPC_IN000001UK01"));
    }

    @Test
    public void shouldCallEhrRepoToStoreMessage() throws HttpException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);

        copcMessageHandler.handleMessage(parsedMessage);
        verify(ehrRepoService).storeMessage(parsedMessage);
    }

    @Test
    public void shouldPutMessageOnUnhandledQueueWhenEhrRepoCallThrows() throws HttpException {
        ParsedMessage parsedMessage = mock(ParsedMessage.class);
        String rawMessage = "test";
        when(parsedMessage.getRawMessage()).thenReturn(rawMessage);

        HttpException expectedError = new HttpException();
        doThrow(expectedError).when(ehrRepoService).storeMessage(parsedMessage);

        copcMessageHandler.handleMessage(parsedMessage);
        verify(jmsProducer, times(1)).sendMessageToQueue(unhandledQueue, rawMessage);
    }
}
