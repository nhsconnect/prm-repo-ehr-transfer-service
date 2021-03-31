package uk.nhs.prm.deductions.gp2gpmessagehandler.handlers;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import uk.nhs.prm.deductions.gp2gpmessagehandler.gp2gpMessageModels.*;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.EhrRepoService;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.GPToRepoClient;
import uk.nhs.prm.deductions.gp2gpmessagehandler.services.HttpException;

import javax.jms.JMSException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@Tag("unit")
/* here we list classes that we want to be instantiated in the test */
@SpringBootTest(classes = { EhrExtractMessageHandler.class })
public class EhrExtractMessageHandlerTest {
    @Autowired
    EhrExtractMessageHandler messageHandler;

    @MockBean
    JmsTemplate mockJmsTemplate;
    @MockBean
    GPToRepoClient gpToRepoClient;
    @MockBean
    EhrRepoService ehrRepoService;

    @Value("${activemq.outboundQueue}")
    String outboundQueue;

    @Value("${activemq.unhandledQueue}")
    String unhandledQueue;

    private UUID conversationId;
    private UUID ehrExtractMessageId;

    public EhrExtractMessageHandlerTest() {
        conversationId = UUID.randomUUID();
        ehrExtractMessageId = UUID.randomUUID();
    }

    private SOAPEnvelope getSoapEnvelope(String href) {
        Reference reference = new Reference();
        reference.href = href;
        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.body = new SOAPBody();
        envelope.body.manifest = new ArrayList<>();
        envelope.body.manifest.add(reference);
        envelope.header = new SOAPHeader();
        envelope.header.messageHeader = new MessageHeader();
        envelope.header.messageHeader.messageData = new MessageData();
        envelope.header.messageHeader.conversationId = conversationId;
        envelope.header.messageHeader.messageData.messageId = ehrExtractMessageId;
        return envelope;
    }

    private ActiveMQBytesMessage getActiveMQBytesMessage() throws JMSException {
        ActiveMQBytesMessage bytesMessage = new ActiveMQBytesMessage();
        bytesMessage.writeBytes(new byte[10]);
        bytesMessage.reset();
        return bytesMessage;
    }

    @Test
    public void shouldReturnCorrectInteractionId() {
        assertThat(messageHandler.getInteractionId(), equalTo("RCMR_IN030000UK06"));
    }

    @Test
    public void shouldPutSmallHealthRecordsOnJSQueue() throws JMSException {
        SOAPEnvelope envelope = getSoapEnvelope("cid:no-attachments");
        ActiveMQBytesMessage bytesMessage = getActiveMQBytesMessage();
        ParsedMessage parsedMessage = new ParsedMessage(envelope, null, bytesMessage);

        messageHandler.handleMessage(parsedMessage);
        verify(mockJmsTemplate, only()).convertAndSend(outboundQueue, bytesMessage);
    }

    @Test
    public void shouldNotPutLargeHealthRecordsOnJSQueue() throws JMSException {
        SOAPEnvelope envelope = getSoapEnvelope("mid:attachment");
        ActiveMQBytesMessage bytesMessage = getActiveMQBytesMessage();
        ParsedMessage parsedMessage = new ParsedMessage(envelope, null, bytesMessage);

        messageHandler.handleMessage(parsedMessage);
        verify(mockJmsTemplate, never()).convertAndSend("outboundQueue", bytesMessage);
    }

    @Test
    public void shouldCallGPToRepoToSendContinueMessageForLargeHealthRecords() throws MalformedURLException, URISyntaxException {
        SOAPEnvelope envelope = getSoapEnvelope("mid:attachment");
        ParsedMessage parsedMessage = new ParsedMessage(envelope, null, null);

        messageHandler.handleMessage(parsedMessage);
        verify(gpToRepoClient).sendContinueMessage(ehrExtractMessageId, conversationId);
    }

    @Test
    public void shouldPutLargeMessageOnUnhandledQueueWhenGPToRepoCallThrows() throws JMSException, MalformedURLException, URISyntaxException {
        SOAPEnvelope envelope = getSoapEnvelope("mid:attachment");
        RuntimeException expectedError = new RuntimeException("Failed to send continue message");
        ActiveMQBytesMessage bytesMessage = getActiveMQBytesMessage();
        ParsedMessage parsedMessage = new ParsedMessage(envelope, null, bytesMessage);
        doThrow(expectedError).when(gpToRepoClient).sendContinueMessage(ehrExtractMessageId, conversationId);

        messageHandler.handleMessage(parsedMessage);
        verify(mockJmsTemplate, times(1)).convertAndSend(unhandledQueue, bytesMessage);
    }

    @Test
    public void shouldCallEhrRepoToStoreMessageForLargeHealthRecords() throws JMSException, HttpException {
        SOAPEnvelope envelope = getSoapEnvelope("mid:attachment");
        EhrExtractMessageWrapper ehrExtractMessageWrapper = getMessageContent("1234567890");
        ActiveMQBytesMessage bytesMessage = getActiveMQBytesMessage();
        ParsedMessage parsedMessage = new ParsedMessage(envelope, ehrExtractMessageWrapper, bytesMessage);

        messageHandler.handleMessage(parsedMessage);
        verify(ehrRepoService).storeMessage(parsedMessage);
    }

    @Test
    public void shouldPutLargeMessageOnUnhandledQueueWhenEhrRepoCallThrows() throws JMSException, HttpException {
        SOAPEnvelope envelope = getSoapEnvelope("mid:attachment");
        EhrExtractMessageWrapper ehrExtractMessageWrapper = getMessageContent("1234567890");
        ActiveMQBytesMessage bytesMessage = getActiveMQBytesMessage();
        ParsedMessage parsedMessage = new ParsedMessage(envelope, ehrExtractMessageWrapper, bytesMessage);

        HttpException expectedError = new HttpException();
        doThrow(expectedError).when(ehrRepoService).storeMessage(parsedMessage);

        messageHandler.handleMessage(parsedMessage);
        verify(mockJmsTemplate, times(1)).convertAndSend(unhandledQueue, bytesMessage);
    }

    private EhrExtractMessageWrapper getMessageContent(String nhsNumber) {
        EhrExtractMessageWrapper ehrExtractMessageWrapper = new EhrExtractMessageWrapper();
        ehrExtractMessageWrapper.controlActEvent = new EhrExtractMessageWrapper.ControlActEvent();
        ehrExtractMessageWrapper.controlActEvent.subject = new EhrExtractMessageWrapper.ControlActEvent.Subject();
        ehrExtractMessageWrapper.controlActEvent.subject.ehrExtract = new EhrExtract();
        ehrExtractMessageWrapper.controlActEvent.subject.ehrExtract.recordTarget = new EhrExtract.RecordTarget();
        ehrExtractMessageWrapper.controlActEvent.subject.ehrExtract.recordTarget.patient = new Patient();
        ehrExtractMessageWrapper.controlActEvent.subject.ehrExtract.recordTarget.patient.id = new Identifier();
        ehrExtractMessageWrapper.controlActEvent.subject.ehrExtract.recordTarget.patient.id.extension= nhsNumber;
        return ehrExtractMessageWrapper;
    }
}
