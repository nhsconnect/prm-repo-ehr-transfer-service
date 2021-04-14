package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.prm.deductions.gp2gpmessagehandler.JmsProducer;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.ParsedMessage;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.EhrRepoService;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.HttpException;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@Tag("unit")
public class CopcMessageHandlerTest {
    JmsProducer jmsProducer = mock(JmsProducer.class);
    EhrRepoService ehrRepoService = mock(EhrRepoService.class);

    @Value("${activemq.outboundQueue}")
    String outboundQueue;

    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;

    CopcMessageHandler copcMessageHandler = new CopcMessageHandler(jmsProducer, ehrRepoService, unhandledQueue);

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
