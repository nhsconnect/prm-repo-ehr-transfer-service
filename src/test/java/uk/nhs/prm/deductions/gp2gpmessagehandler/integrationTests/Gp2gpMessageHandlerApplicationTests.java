package uk.nhs.prm.deductions.gp2gpmessagehandler.integrationTests;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import de.mkammerer.wiremock.WireMockExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
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

    @Value("${activemq.unhandledQueue}")
    private String unhandledQueue;

    private TestDataLoader dataLoader = new TestDataLoader();

    @Test
    void shouldProcessAndStoreJsonFormattedSmallEhrExtract() throws IOException, InterruptedException {
        String smallEhrExtract = dataLoader.getDataAsString("RCMR_IN030000UK06");
        String url = String.format("%s/ehr-storage", wireMock.baseUrl());
        wireMock.stubFor(get(anyUrl()).willReturn(aResponse().withBody(url).withStatus(200)));
        wireMock.stubFor(put(urlMatching("/ehr-storage")).willReturn(aResponse().withStatus(200)));
        wireMock.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(201)));
        wireMock.stubFor(patch(anyUrl()).willReturn(aResponse().withStatus(204)));

        jmsTemplate.send(inboundQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes(smallEhrExtract.getBytes(StandardCharsets.UTF_8));
                return bytesMessage;
            }
        });
        sleep(5000);
        verify(getRequestedFor(urlMatching("/messages/ff27abc3-9730-40f7-ba82-382152e6b90a/1c66bb0e-811e-4956-8f9c-33424695b75f")));
        verify(putRequestedFor(urlMatching("/ehr-storage")).withRequestBody(com.github.tomakehurst.wiremock.client.WireMock.equalTo(smallEhrExtract)));
        verify(postRequestedFor(urlMatching("/messages")));
        verify(patchRequestedFor(urlMatching("/deduction-requests/ff27abc3-9730-40f7-ba82-382152e6b90a/ehr-message-received")));
        jmsTemplate.setReceiveTimeout(1000);
        assertNull(jmsTemplate.receive(unhandledQueue));
    }

    @Test
    void shouldUploadLargeEhrExtractToEhrRepoStorage() throws IOException, InterruptedException {
        String largeEhrExtract = dataLoader.getDataAsString("RCMR_IN030000UK06WithMid");
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
        verify(getRequestedFor(urlMatching("/messages/ff27abc3-9730-40f7-ba82-382152e6b90a/1c66bb0e-811e-4956-8f9c-33424695b75f")));
        verify(putRequestedFor(urlMatching("/ehr-storage")).withRequestBody(com.github.tomakehurst.wiremock.client.WireMock.equalTo(largeEhrExtract)));
        verify(postRequestedFor(urlMatching("/messages")));
        verify(patchRequestedFor(urlMatching("/deduction-requests/ff27abc3-9730-40f7-ba82-382152e6b90a/large-ehr-started")));
        jmsTemplate.setReceiveTimeout(1000);
        assertNull(jmsTemplate.receive(unhandledQueue));
    }

    @Test
    void shouldUploadAttachmentMessageToEhrRepoStorage() throws IOException, InterruptedException {
        String copcMessage = dataLoader.getDataAsString("COPC_IN000001UK01");
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
        String pdsUpdatedMessage = dataLoader.getDataAsString("PRPA_IN000202UK01");
        String url = String.format("/deduction-requests/%s/pds-updated", "723c5f3a-1ab8-4515-a582-3e5cc600bf59");
        wireMock.stubFor(patch(urlMatching(url)).willReturn(aResponse().withStatus(204)));

        jmsTemplate.send(inboundQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes(pdsUpdatedMessage.getBytes(StandardCharsets.UTF_8));
                return bytesMessage;
            }
        });
        sleep(5000);
        verify(patchRequestedFor(urlMatching(url)).withHeader("Authorization", new EqualToPattern("auth-key-1")));
        jmsTemplate.setReceiveTimeout(1000);
        assertNull(jmsTemplate.receive(unhandledQueue));
    }

    @Test
    void shouldSendRegistrationRequestToRepoToGp() throws IOException, InterruptedException {
        String registrationRequestMessage = dataLoader.getDataAsString("RCMR_IN010000UK05");
        String conversationId = "17a757f2-f4d2-444e-a246-9cb77bef7f22";
        String ehrRequestId = "FFFB3C70-0BCC-4D9E-A441-7E9C41A897AA";
        String odsCode = "A91720";
        String nhsNumber = "9692842304";

        String requestBody = "{\"data\":{\"type\":\"registration-requests\",\"id\":\"" + conversationId + "\",\"attributes\":{\"ehrRequestId\":\"" + ehrRequestId + "\",\"odsCode\":\"" + odsCode + "\",\"nhsNumber\":\"" + nhsNumber + "\"}}}";
        wireMock.stubFor(post(urlMatching("/registration-requests")).willReturn(aResponse().withStatus(204)));

        jmsTemplate.send(inboundQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes(registrationRequestMessage.getBytes(StandardCharsets.UTF_8));
                return bytesMessage;
            }
        });
        sleep(5000);
        verify(postRequestedFor(urlMatching("/registration-requests")).withRequestBody(equalToJson(requestBody)));
        jmsTemplate.setReceiveTimeout(1000);
        assertNull(jmsTemplate.receive(unhandledQueue));
    }
}