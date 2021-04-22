package uk.nhs.prm.deductions.gp2gpmessagehandler.integrationTests;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import de.mkammerer.wiremock.WireMockExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import uk.nhs.prm.deductions.gp2gpmessagehandler.utils.TestDataLoader;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("integration") // perhaps we need other name for tests that interact with external systems
@SpringBootTest
class Gp2gpMessageHandlerApplicationTests {
    @RegisterExtension
    WireMockExtension wireMock = new WireMockExtension();

    @Autowired
    JmsTemplate jmsTemplate;

    @Value("${activemq.inboundQueue}")
    private String inboundQueue;

    @Value("${activemq.outboundQueue}")
    private String outboundQueue;

    @Value("${activemq.unhandledQueue}")
    private String unhandledQueue;

    private TestDataLoader dataLoader = new TestDataLoader();

    @Test
    void shouldUploadLargeEhrExtractToEhrRepoStorage() throws IOException, InterruptedException {
        String largeEhrExtract = dataLoader.getDataAsString("ehrOneLargeMessage.xml");
        String url = String.format("%s/ehr-storage", wireMock.baseUrl());
        wireMock.stubFor(get(anyUrl()).willReturn(aResponse().withBody(url).withStatus(200)));
        wireMock.stubFor(put(urlMatching("/ehr-storage")).willReturn(aResponse().withStatus(200)));
        wireMock.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(201)));
        wireMock.stubFor(patch(anyUrl()).willReturn(aResponse().withStatus(204)));

        jmsTemplate.send(inboundQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes(largeEhrExtract.getBytes(StandardCharsets.UTF_8));
                return bytesMessage;
            }
        });
        sleep(5000);
        verify(putRequestedFor(urlMatching("/ehr-storage")).withRequestBody(com.github.tomakehurst.wiremock.client.WireMock.equalTo(largeEhrExtract)));
        jmsTemplate.setReceiveTimeout(1000);
        assertNull(jmsTemplate.receive(unhandledQueue));
    }

    @Test
    void shouldUploadAttachmentMessageToEhrRepoStorage() throws IOException, InterruptedException {
        String copcMessage = dataLoader.getDataAsString("COPC_IN000001UK01.xml");
        String url = String.format("%s/attachment-storage", wireMock.baseUrl());
        wireMock.stubFor(get(anyUrl()).willReturn(aResponse().withBody(url).withStatus(200)));
        wireMock.stubFor(put(urlMatching("/attachment-storage")).willReturn(aResponse().withStatus(200)));
        wireMock.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(201)));

        jmsTemplate.send(inboundQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes(copcMessage.getBytes(StandardCharsets.UTF_8));
                return bytesMessage;
            }
        });
        sleep(5000);
        verify(putRequestedFor(urlMatching("/attachment-storage")).withRequestBody(com.github.tomakehurst.wiremock.client.WireMock.equalTo(copcMessage)));
        jmsTemplate.setReceiveTimeout(1000);
        assertNull(jmsTemplate.receive(unhandledQueue));
    }

    @Test
    void shouldCallGpToRepoWhenReceivedPdsUpdateCompleted() throws IOException, InterruptedException {
        String copcMessage = dataLoader.getDataAsString("PRPA_IN000202UK01");
        String url = String.format("%s/deduction-requests/%s/pds-updated", wireMock.baseUrl(), "3B71EB7E-5F87-426D-AE23-E0EAFEB60BD4");
        wireMock.stubFor(patch(urlMatching(url)).willReturn(aResponse().withStatus(204)));

        jmsTemplate.send(inboundQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes(copcMessage.getBytes(StandardCharsets.UTF_8));
                return bytesMessage;
            }
        });
        sleep(5000);
        verify(patchRequestedFor(urlMatching(url)).withHeader("Authorization", new EqualToPattern("gp-to-repo-auth-key")));
        jmsTemplate.setReceiveTimeout(1000);
        assertNull(jmsTemplate.receive(unhandledQueue));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "RCMR_IN030000UK06.xml", // small EHR extract
            "PRPA_IN000202UK01.xml" // PDS update
    })
    void shouldSendMessageWithKnownInteractionIdsToOldWorker(String fileName) throws IOException, JMSException {
        String ehrRequest = dataLoader.getDataAsString(fileName);
        jmsTemplate.send(inboundQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes(ehrRequest.getBytes(StandardCharsets.UTF_8));
                return bytesMessage;
            }
        });
        jmsTemplate.setReceiveTimeout(5000);
        BytesMessage message = (BytesMessage) jmsTemplate.receive(outboundQueue);
        assertNotNull(message);
        byte[] allTheBytes = new byte[(int) message.getBodyLength()];
        message.readBytes(allTheBytes);
        String messageAsString = new String(allTheBytes, StandardCharsets.UTF_8);
        assertThat(messageAsString, equalTo(ehrRequest));
    }
}